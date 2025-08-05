package client

import candles.{Candle, CandleSize, Pair}
import cats.*
import cats.effect.*
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.http4s.implicits.uri
import utils.DateTimeUtils.encodeDateTime

import java.time.LocalDateTime

trait OkxApi {

  protected val GET_HISTORY_CANDLES: Uri = uri"https://okx.com/api/v5/market/history-index-candles"
  protected val GET_CANDLES: Uri = uri"https://okx.com/api/v5/market/index-candles"


  def getHistoryCandles(client: Client[IO])(
      ts: LocalDateTime,
      pair: Pair | String,
      candleSize: CandleSize | String,
      limit: Int = 1000
  ): IO[List[Candle]] = ???

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

}
