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
import db.CandleTableStatus
import scala.util.Try

trait CandleService extends OkxApiCandle with CandleTable:

  def update(candleType: CandleType, httpClient: Client[IO], tx: Transactor[IO]): IO[Unit] = for {
    status: List[CandleTableStatus] <- getCandleTableStatus(candleType).transact(tx)
    (latest, others) <- IO.fromTry(splitTableStatuses(status))
    _ <- downladAndSave(latest.ts, LocalDateTime.now, candleType, httpClient, tx)
    _ <- others.traverse_ { (s: CandleTableStatus) => downladAndSave(s.ts, s.ts2.get, candleType, httpClient, tx) }
    status <- getCandleTableStatus(candleType).transact(tx)
    errors = Eval.later(status.mkString(","))
    _ <- IO.raiseWhen(status.length != 1)(
      new Exception(s"Error updating candleType=${candleType} errors=${errors.value}!")
    )
  } yield ()

  def splitTableStatuses(l: List[CandleTableStatus]): Try[(CandleTableStatus, List[CandleTableStatus])] = Try {
    val (other, latestGap) = l.span { s => !(s.ts2.isEmpty && s.duration.isEmpty) }
    assert(latestGap.length == 1)
    latestGap.head -> other
  }

  def downladAndSave(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    candleType: CandleType,
    httpClient: Client[IO],
    tx: Transactor[IO]
  ): IO[Unit] =
    assert(startTime < endTime)
    for {
      _ <- IO.println(s"Starting downladAndSave(start=$startTime, end=$endTime, candleType=$candleType)")
      _ <- createCandleTable(candleType).transact(tx)
      candles <- getCandleStream(httpClient, GET_HISTORY_CANDLES)(startTime, endTime, candleType)
        .evalTap { (c: List[Candle]) =>
          val m =
            s"Downloaded bunch minTs=${c.map(_.ts).min} maxTs=${c.map(_.ts).max} length=${c.length} candleType=$candleType"
          IO.println(m)
        }
        .evalMap { (cs: List[Candle]) => upsertCandle(candleType, cs).transact(tx) }
        .compile
        .toList
    } yield ()

  end downladAndSave
