package client

import candles.{Candle, CandleSize, Pair}
import cats.*
import cats.effect.*
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.http4s.client.middleware.FollowRedirect
import org.http4s.implicits.{path, uri}
import org.http4s.netty.client.NettyClientBuilder
import utils.DateTimeUtils.encodeDateTime

import java.time.LocalDateTime
import scala.concurrent.duration.*

trait OkxApi {

  protected val GET_HISTORY_CANDLES: Uri = uri"https://okx.com/api/v5/market/history-index-candles"
  protected val GET_CANDLES: Uri = uri"https://okx.com/api/v5/market/index-candles"

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
  ): IO[List[Candle]] = ???

  def getCandles(client: Client[IO], uriApi: Uri)(
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
    val uri: Uri = uriApi.withQueryParams(params)
    for {
      json: Json <- client.expect[Json](uri)
      response: GetCandlesResponse <- IO.fromEither(json.as[GetCandlesResponse])
      candles: List[Candle] <- IO.fromTry(response.toCandles)
    } yield candles
  }

}
