package client

import candles.{Candle, CandleSize, Pair}
import cats.*
import cats.effect.*
import cats.syntax.traverse.*
import doobie.ConnectionIO
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.http4s.client.middleware.FollowRedirect
import org.http4s.implicits.{path, uri}
import org.http4s.netty.client.NettyClientBuilder
import utils.DateTimeUtils.{decodeDateTime, encodeDateTime}

import java.time.LocalDateTime
import scala.concurrent.duration.*
import scala.util.Try

case class GetCandlesResponse(
    code: Option[String],
    msg: Option[String],
    data: List[List[String]]
) {
  def toCandles: Try[List[Candle]] = data.traverse(convertToCandle)

  private def convertToCandle(xs: Seq[String]): Try[Candle] = for {
    _ <- Try(assert(xs.length == 6, "Invalid candle data!"))
    ts <- decodeDateTime(xs.head)
    o <- parseBigDecimal(xs(1))
    h <- parseBigDecimal(xs(2))
    l <- parseBigDecimal(xs(3))
    c <- parseBigDecimal(xs(4))
  } yield Candle(ts, o, h, l, c, xs(5))

  private def parseBigDecimal(s: String): Try[BigDecimal] = Try(BigDecimal(s))

}

trait OkxApi {

  private val host: Uri = uri"https://okx.com"
  protected val GET_HISTORY_CANDLES: Uri.Path = path"/api/v5/market/history-index-candles"
  protected val GET_CANDLES: Uri.Path = path"/api/v5/market/index-candles"

  def httpClient: Resource[IO, Client[IO]] =
    NettyClientBuilder[IO]
      .withIdleTimeout(10.seconds)
      .resource
      .map(FollowRedirect(5))

  def getHistoryCandles(client: Client[IO])(
      ts: LocalDateTime,
      pair: Pair | String,
      candleSize: CandleSize | String,
      limit: Int = 1000
  ): IO[List[Candle]] = {
    val instId = pair match {
      case (x, y)    => s"$x-$y"
      case s: String => s
    }
    val bar = candleSize match {
      case cs: CandleSize => cs.toString
      case s: String      => s
    }
    val uri: Uri = host
      .withPath(GET_HISTORY_CANDLES)
      .withQueryParams(
        Map(
          "instId" -> instId,
          "after" -> encodeDateTime(ts),
          "bar" -> bar,
          "limit" -> limit.toString
        )
      )
    for {
      json: Json <- client.expect[Json](uri)
      response: GetCandlesResponse <- IO.fromEither(json.as[GetCandlesResponse])
      candles: List[Candle] <-  IO.fromTry(response.toCandles)
    } yield candles
  }

  def getCandles(client: Client[IO], apiPath: Uri.Path)(
    after: Option[LocalDateTime],
    before: Option[LocalDateTime],
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
    val uri: Uri = host.withPath(apiPath).withQueryParams(params)
    for {
      json: Json <- client.expect[Json](uri)
      response: GetCandlesResponse <- IO.fromEither(json.as[GetCandlesResponse])
      candles: List[Candle] <- IO.fromTry(response.toCandles)
    } yield candles
  }

}
