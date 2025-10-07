package example

import candles._
import cats._
import cats.effect._
import db.DoobieTransactor
import doobie._
import man.CandleService
import org.http4s.client.Client
import org.http4s.client.middleware.FollowRedirect
import org.http4s.netty.client.NettyClientBuilder

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone
import scala.concurrent.duration._

object Main
    extends IOApp.Simple
    with CandleService
    with DoobieTransactor:

  private val resources: Resource[IO, (Client[IO], Transactor[IO])] = for {
    httpClient: Client[IO] <- NettyClientBuilder[IO]
      .withIdleTimeout(10.seconds)
      .resource
      .map(FollowRedirect(5))
    tx: Transactor[IO] <- transactor
  } yield httpClient -> tx

  def run: IO[Unit] = {
    val startTime: LocalDateTime = LocalDateTime.parse("2021-01-01T00:00")
    val endTime = LocalDateTime.parse("2022-01-10T07:00")
    val candleType: CandleType = (pair = (Currency.BTC, Currency.USDT), candleSize = CandleSize.`1m`)
    IO(TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")))) >>
      resources.use { (httpClient, tx) =>
        update(candleType, httpClient, tx)
      }
  }
end Main
