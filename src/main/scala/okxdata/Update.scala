package okxdata

import candles._
import cats._
import cats.effect._
import cats.syntax.all._
import db.DoobieTransactor
import doobie._
import man.CandleService
import org.http4s.client.Client
import org.http4s.client.middleware.FollowRedirect
import org.http4s.netty.client.NettyClientBuilder

import java.time.ZoneId
import java.util.TimeZone
import scala.concurrent.duration._

object Update
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
    val candleType = CandleType((Currency.BTC, Currency.USDT), CandleSize.`1m`)
    val candleType2= CandleType((Currency.BTC, Currency.USDT), CandleSize.`1H`)
    val candleType3= CandleType((Currency.BTC, Currency.USDT), CandleSize.`1Dutc`)
    IO(TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")))) >>
      resources.use { (httpClient, tx) =>
        List(candleType, candleType2, candleType3).traverse_(ct=> update(ct, httpClient, tx))
      }
  }
end Update
