package man
import candles._
import cats._
import cats.effect._
import cats.implicits._
import client.OkxApiCandle
import db.CandleTable
import db.CandleTableStatus
import doobie._
import doobie.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser.{parse => parseJson}
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.websocket.WSFrame.Text
import org.http4s.client.websocket._
import org.http4s.implicits.uri
import org.http4s.netty.client.NettyWSClientBuilder

import java.time.LocalDateTime
import scala.math.Ordered.orderingToOrdered
import scala.util.Try

import concurrent.duration._

trait CandleService extends OkxApiCandle with CandleTable:

  def update(candleType: CandleType, httpClient: Client[IO], tx: Transactor[IO]): IO[Unit] = for {
    _ <- initTableIfNotExists(candleType, httpClient, tx)
    status: List[CandleTableStatus] <- getCandleTableStatus(candleType).transact(tx)
    (latest, others) <- IO.fromTry(splitTableStatuses(status))
    _ <- downloadAndSave(latest.ts, LocalDateTime.now, candleType, httpClient, tx)
    _ <- others.traverse_ { (s: CandleTableStatus) => // noinspection IllegalOptionGet
      downloadAndSave(s.ts, s.ts2.get, candleType, httpClient, tx)
    }
    status <- getCandleTableStatus(candleType).transact(tx)
    errors = Eval.later(status.mkString(","))
    _ <- IO.raiseWhen(status.length != 1)(
      new Exception(s"Error updating $candleType errors=${errors.value}!")
    )
  } yield ()

  // noinspection SpellCheckingInspection
  def websocketUpdate(
    candleTypes: List[CandleType],
    killSwitch: Ref[IO, Boolean],
    tx: Transactor[IO],
    wsUri: Uri = uri"wss://ws.okx.com:8443/ws/v5/business"
  ): IO[Unit] =
    //  val wsUri = uri"wss://wspap.okx.com:8443/ws/v5/business"
    // val wsUri: Uri = uri"wss://ws.okx.com:8443/ws/v5/business" //nb!!!

    def upsertFrame(wsFrame: WSFrame): IO[Unit] =
      println(s"upsertFrame($wsFrame) ...")
      def upsert(c: Candle): IO[Unit] =
        // IO.whenA(c.confirm == "1")(upsertCandle(candleType, List(c)).transact(tx).as(()))
        IO.println(s"upsertFrame($c): OK")

      val candles: Either[Throwable, List[Candle]] = {
        Either.cond[Throwable, String](
          wsFrame.isInstanceOf[Text],
          wsFrame.asInstanceOf[Text].data,
          new Exception(s"Error in upsertFrame($wsFrame): Text frame expected !") // todo candleType
        )
          .flatMap(parseJson)
          .flatMap(_.as[WSCandleFrame])
          .flatMap(_.data.traverse(Candle.fromStringsBrief).toEither)
          .handleErrorWith { e =>
            println(s"Error  in upsertFrame($wsFrame): ${e.getMessage} !"); Left(e)
          }
      }
      // noinspection IllegalOptionGet
      IO.whenA(candles.isRight)(candles.toOption.get.traverse(upsert).as(()))
    end upsertFrame

    val subscribe: WSSubscribe = WSSubscribe(candleTypes)
    val unsubscribe: WSUnsubscribe = WSUnsubscribe(subscribe)
    NettyWSClientBuilder[IO].withIdleTimeout(5.seconds).resource.use { (c: WSClient[IO]) =>
      c.connectHighLevel(WSRequest(wsUri)).use { (wsConnection: WSConnectionHighLevel[IO]) =>
        def unsubscribeCheck: IO[Boolean] = for {
          stop <- killSwitch.get
          _ <- IO.whenA(stop) {
            wsConnection.send(unsubscribe.asFrame)
              >> IO.println(s"KillSwitch is ON! unsubscribe=${unsubscribe.asFrame.toString}")
          }
        } yield stop

        IO.println("Using wsConnection ...")
          >> IO.println(s"Subscribing ${subscribe.asFrame.toString} ...")
          >> wsConnection.send(subscribe.asFrame)
          >> wsConnection.receive.map("First frame: " + _.toString).map(IO.println)
          >> wsConnection.receiveStream
            .evalMap(upsertFrame).evalMap(_ => unsubscribeCheck)
            .takeWhile(_ == false)
            .compile.drain
          >> IO.println("End wsConnection")
      } >> IO.println("WSClient close")
    }
  end websocketUpdate

  private def splitTableStatuses(l: List[CandleTableStatus]): Try[(CandleTableStatus, List[CandleTableStatus])] = Try {
    val (other, latestGap) = l.span { s => !(s.ts2.isEmpty && s.duration.isEmpty) }
    lazy val errorMsg = s"Ivalid CandleTableStatus - found more then one empty gap! latestGap=$latestGap"
    assert(latestGap.length <= 1, errorMsg)
    latestGap.head -> other
  }

  def downloadAndSave(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    candleType: CandleType,
    httpClient: Client[IO],
    tx: Transactor[IO]
  ): IO[Unit] =
    assert(startTime < endTime)
    for {
      _ <- IO.println(s"Starting downloadAndSave(start=$startTime, end=$endTime, $candleType)")
      _ <- createCandleTable(candleType).transact(tx)
        .recoverWith { case e => IO.println(s"Error createCandleTable($candleType): ${e.getMessage} !") >> IO.raiseError(e) }
      candles <- getCandleStream(httpClient, GET_HISTORY_CANDLES)(startTime, endTime, candleType)
        // .evalTap { (c: List[Candle]) =>
        //   val m = s"Downloaded ${c.length} candles [${c.map(_.ts).min}, ${c.map(_.ts).max}], $candleType."
        //   IO.println(m)
        // }
        .evalMap { (cs: List[Candle]) => upsertCandle(candleType, cs).transact(tx) }
        .compile
        .toList
    } yield ()

  end downloadAndSave

  def initTableIfNotExists(candleType: CandleType, httpClient: Client[IO], tx: Transactor[IO], numberOfPeriods: Int = 301): IO[Unit] =
    val tableName = candleType.candleTableName
    val queryTableExists: ConnectionIO[Option[Int]] =
         sql""" SELECT 1 
              |   FROM pg_tables 
              |   WHERE schemaname = 'public' AND
              |         tablename = $tableName;""".stripMargin.query[Int].option

    for 
      tableExists <- queryTableExists.transact(tx)
      _ <- IO.whenA(tableExists.isEmpty) {
              val startTime = LocalDateTime.now.minusSeconds((candleType.candleSize.duration * numberOfPeriods).toSeconds)
              IO.println(s"Initilazing empty table for $candleType") >>
                downloadAndSave(startTime, LocalDateTime.now, candleType, httpClient, tx)
      }
    yield ()
  end initTableIfNotExists

end CandleService
