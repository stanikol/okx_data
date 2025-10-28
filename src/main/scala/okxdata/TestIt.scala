import candles.CandleSize
import candles.Currency
import cats._
import cats.effect._
import db.DoobieTransactor
import doobie._
import man.CandleService
import org.http4s.client.websocket._
import org.http4s.netty.client._

import scala.concurrent.duration._

object TestIt extends IOApp.Simple with CandleService with DoobieTransactor:
////  val wsUri = uri"wss://wspap.okx.com:8443/ws/v5/business"
//  val wsUri: Uri = uri"wss://ws.okx.com:8443/ws/v5/business"
  import candles.CandleType

  val wsClient: Resource[IO, WSClient[IO]] = NettyWSClientBuilder[IO].withIdleTimeout(5.seconds).resource

  def stop(ref: Ref[IO, Boolean]): IO[Unit] = {
    IO.readLine
      >> ref.set(true)
      >> IO.println("BYE!")
  }

  override def run: IO[Unit] =
    val candleType: CandleType = (pair = (Currency.BTC, Currency.USDT), candleSize = CandleSize.`1m`)

    val resource: Resource[IO, (WSClient[IO], Transactor[IO])] =
      for (w <- wsClient; t <- transactor) yield w -> t

    resource.use { (wsClient, tx: Transactor[IO]) =>
      for {
        killSwitch <- Ref.of[IO, Boolean](false)
        killProc <- stop(killSwitch).start
        _ <- websocketUpdate(candleType, wsClient, killSwitch, tx)
        _ <- killProc.join
      } yield ExitCode.Success
    }

  end run

end TestIt
