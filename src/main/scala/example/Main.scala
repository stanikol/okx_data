package example

import candles.CandleSize.*
import candles.Tickers
import cats.*
import cats.effect.*
import cats.implicits.*
import client.OkxApiCandle
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
    with OkxApiCandle {

  private val resources: Resource[IO, (Client[IO], Transactor[IO])] = for {
    applicationConf: ApplicationConf <- Resource.liftK(
      IO.fromEither(conf.ApplicationConf.apply())
    )
    httpClient <- httpClient
    tx <- transactor(applicationConf.db)
  } yield httpClient -> tx

  def run: IO[Unit] = {
    val end = LocalDateTime.now(ZoneId.of("UTC"))
    val start = end.minusHours(15)
    assert(start < end)
    resources.use { (httpClient, tx) =>
      for {
        _ <- IO.println(s"start=$start end=$end")
        _ <- sql"drop table if exists delme".update.run.transact(tx)
        candles <- getCandleStream(httpClient, GET_HISTORY_CANDLES)(
          start,
          end,
          Tickers.BTC -> Tickers.USDT,
          `1m`
        ).evalTap(c =>
          IO.println(s"Step ${c.map(_.ts).min} ${c.map(_.ts).max} ${c.length}")
        ).take(10)
          .compile
          .toList
        c = candles.flatten
        _ <- IO.println(
          s"Final ${c.map(_.ts).min} ${c.map(_.ts).max} ${c.length}"
        )
        _ <- IO.println(s"")
//        _ <- IO.println(candles.head, candles.last, candles.length)
//        _ <- (createCandleTable("delme") >> insertCandles("delme", candles)).transact(tx)
//        _ <- IO.println(":)")
//        candles2 <- selectCandles("delme").transact(tx)
//        _ <- IO.println(candles2.head, candles2.last, candles2.length)
      } yield ()
    }
  }
}
