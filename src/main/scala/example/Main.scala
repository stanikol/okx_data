package example

import candles.CandleSize.*
import candles.{Candle, Tickers}
import cats.*
import cats.effect.*
import cats.implicits.*
import client.OkxApi
import conf.ApplicationConf
import db.{CandleTable, DoobieTransactor}
import doobie.*
import doobie.implicits.*
import org.http4s.client.Client

import java.time.{LocalDateTime, ZoneId}
import scala.math.Ordered.orderingToOrdered

object Main
    extends IOApp.Simple
    with CandleTable
    with DoobieTransactor
    with OkxApi {

  private val resources: Resource[IO, (Client[IO], Transactor[IO])] = for {
    c: ApplicationConf <- Resource.liftK(
      IO.fromEither(conf.ApplicationConf.apply())
    )
    httpClient <- httpClient
    tx <- transactor(c.okx_data.db)
  } yield httpClient -> tx

  def run: IO[Unit] = {
    val end = LocalDateTime.now(ZoneId.of("UTC")).minusDays(1)
    val start = end.minusHours(1)
    assert(start < end)
    val after = Some(end)
    val before = Some(start)
    resources.use { case (httpClient, tx) =>
      for {
        _ <- sql"drop table if exists delme".update.run.transact(tx)
        candles: List[Candle] <- getCandles(httpClient, GET_HISTORY_CANDLES)(
          after,
          before,
          Tickers.BTC -> Tickers.USDT,
          `1m`,
          limit = 100
        )
        _ <- IO.println(candles.head, candles.last, candles.length)
        _ <- (createCandleTable("delme") >> insertCandles("delme", candles)).transact(tx)
        _ <- IO.println(":)")
        candles2 <- selectCandles("delme").transact(tx)
        _ <- IO.println(candles2.head, candles2.last, candles2.length)
      } yield ()
    }
  }
}
