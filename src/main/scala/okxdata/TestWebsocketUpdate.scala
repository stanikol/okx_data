package okxdata
import candles.CandleSize
import candles.CandleType
import candles.Currency
import cats._
import cats.effect._
import db.DoobieTransactor
import doobie._
import man.CandleService
import org.http4s.client.websocket._
import org.http4s.netty.client._

import scala.concurrent.duration._

object TestWebsocketUpdate extends IOApp.Simple with CandleService with DoobieTransactor:

  def wsClient: Resource[IO, WSClient[IO]] = NettyWSClientBuilder[IO].withIdleTimeout(5.seconds).resource

  def stop(ref: Ref[IO, Boolean]): IO[Unit] = {
    IO.readLine
      >> ref.set(true)
      >> IO.println("BYE!")
  }

  override def run: IO[Unit] =
    val candleTypes: List[CandleType] = List(
      CandleType((Currency.BTC, Currency.USDT), CandleSize.`1m`),
      CandleType((Currency.XRP, Currency.USDT), CandleSize.`1m`)
    )

    transactor.use { ( tx: Transactor[IO]) =>
      for {
        killSwitch <- Ref.of[IO, Boolean](false)
        killProc <- stop(killSwitch).start
        update <- websocketUpdate(candleTypes, killSwitch, tx).start
        _ <-IO.println(11)
        _ <- killProc.join
        _ <- IO.println(22)
        _ <- update.join
        _ <- IO.println(33)
      } yield ExitCode.Success
    }
  end run

end TestWebsocketUpdate
