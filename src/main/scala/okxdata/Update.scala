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

  import CandleTypes.candleTypes

  private val resources: Resource[IO, (Client[IO], Transactor[IO])] = for {
    httpClient: Client[IO] <- NettyClientBuilder[IO]
      .withIdleTimeout(10.seconds)
      .resource
      .map(FollowRedirect(5))
    tx: Transactor[IO] <- transactor
  } yield httpClient -> tx

  def run: IO[Unit] = {
    IO(TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")))) >>
      resources.use { (httpClient, tx) =>
        candleTypes.traverse_((ct: CandleType) => update(ct, httpClient, tx))
      }
  }
end Update
