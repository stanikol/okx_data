package man
import candles._
import cats._
import cats.effect._
import cats.implicits._
import client.OkxApiCandle
import db.CandleTable
import doobie._
import doobie.implicits._
import org.http4s.client.Client

import java.time.LocalDateTime
import scala.math.Ordered.orderingToOrdered

trait CandleService extends OkxApiCandle with CandleTable:

  def update(candleType: CandleType, httpClient: Client[IO], tx: Transactor[IO]): IO[Unit] = for {
    status <- getCandleTableStatus(candleType).transact(tx)
    latest = status.filter(s => s.ts2.isEmpty && s.duration.isEmpty).headOption
    _ <- IO.raiseWhen(latest.isEmpty)(new Exception("Invalid data from getCandleTableStatus!"))
    _ <- downladAndSave(latest.get.ts, LocalDateTime.now, candleType, httpClient, tx)
    status <- getCandleTableStatus(candleType).transact(tx)
    errors = Eval.later(status.mkString(","))
    _ <- IO.raiseWhen(status.length != 1)(
      new Exception(s"Error updating candleType=${candleType} errors=${errors.value}!")
    )
  } yield ()

  def downladAndSave(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    candleType: CandleType,
    httpClient: Client[IO],
    tx: Transactor[IO]
  ): IO[Unit] =
    assert(startTime < endTime)
    for {
      _ <- IO.println(s"start=$startTime end=$endTime")
      _ <- createCandleTable(candleType).transact(tx)
      candles <- getCandleStream(httpClient, GET_HISTORY_CANDLES)(startTime, endTime, candleType)
        .evalTap(c =>
          IO.println(s"Downloaded bunch minTs=${c.map(_.ts).min} maxTs=${c.map(_.ts).max} length=${c.length}")
        )
        .evalMap { (cs: List[Candle]) => upsertCandle(candleType, cs).transact(tx) }
        .compile
        .toList
    } yield ()

  end downladAndSave
