package client

import candles.{Candle, CandleSize, Pair}
import cats.*
import cats.syntax.all.*
import cats.effect.*
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.http4s.implicits.uri
import utils.DateTimeUtils.encodeDateTime

import java.time.LocalDateTime
import scala.math.Ordered.orderingToOrdered

trait OkxApiCandle {

  protected val GET_HISTORY_CANDLES: Uri = uri"https://okx.com/api/v5/market/history-index-candles"
  protected val GET_CANDLES: Uri = uri"https://okx.com/api/v5/market/index-candles"

  /**
   *
   * @param after - END of period
   * @param before - START of period
   */
  def getCandles(client: Client[IO], uriApi: Uri)(
    before: Option[LocalDateTime],
    after: Option[LocalDateTime],
    pair: Pair | String,
    candleSize: CandleSize | String,
    limit: Int = 1000
  ): IO[List[Candle]] = {
    val instId = pair match {
      case (x, y) => s"$x-$y"
      case s: String => s
    }
    val bar = candleSize match {
      case cs: CandleSize => cs.toString
      case s: String => s
    }
    val params = Map(
      "instId" -> instId,
      "bar" -> bar,
      "limit" -> limit.toString
    ) ++ after.map(ts => "after" -> encodeDateTime(ts))
      ++ before.map(ts => "before" -> encodeDateTime(ts))
    val uri: Uri = uriApi.withQueryParams(params)
    for {
      json: Json <- client.expect[Json](uri)
      response: GetCandlesResponse <- IO.fromEither(json.as[GetCandlesResponse])
      candles: List[Candle] <- IO.fromTry(response.toCandles)
    } yield candles
  }


  def getCandleStream(client: Client[IO], apiUri: Uri)(start: LocalDateTime, end: LocalDateTime,
                                                       pair: Pair | String,
                                                       candleSize: CandleSize | String): fs2.Stream[IO, List[Candle]] = {
    fs2.Stream.unfoldEval((start, end)) {
      case (start, end) if start >= end => IO.pure(Option.empty)
      case (start, end) => for {
        candles <- getCandles(client, apiUri)(start.some, end.some, pair, candleSize, limit = 10000)
        next  <- if(candles.nonEmpty) {
          // endpoint gives data in reverse order, like n-rows from the latest (end) parameter. From end to start to end.
          val (minTs, maxTs) = candles.map(_.ts).min -> candles.map(_.ts).max
          val (newStart, newEnd) = start -> List(minTs, end).min
          Option.when(newStart <= newEnd)(candles, newStart -> newEnd).pure[IO]
        } else {
          IO.println(s"Warning: Empty response from endpoint in getCandleStream(start=$start, end=$end, pair=$pair, candleSize=$candleSize, apiUri=$apiUri)!") >>
          Option.empty.pure[IO]
        }
      } yield next
      }
  }
}
