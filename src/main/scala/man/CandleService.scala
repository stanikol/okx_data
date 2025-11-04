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
    status: List[CandleTableStatus] <- getCandleTableStatus(candleType).transact(tx)
    (latest, others) <- IO.fromTry(splitTableStatuses(status))
    _ <- downloadAndSave(latest.ts, LocalDateTime.now, candleType, httpClient, tx)
    _ <- others.traverse_ { (s: CandleTableStatus) => downloadAndSave(s.ts, s.ts2.get, candleType, httpClient, tx) }
    status <- getCandleTableStatus(candleType).transact(tx)
    errors = Eval.later(status.mkString(","))
    _ <- IO.raiseWhen(status.length != 1)(
      new Exception(s"Error updating candleType=$candleType errors=${errors.value}!")
    )
  } yield ()

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
        IO.unit

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
            println(s"Error ${e.getMessage}"); Left(e)
          }
      }
      IO.whenA(candles.isRight)(candles.toOption.get.traverse(upsert).as(()))
        >> IO.println(s"OK: upsertFrame($wsFrame): Finished")
    end upsertFrame

    val subscribe: WSSubscribe = WSSubscribe(candleTypes)
    val unsubscribe: WSUnsubscribe = WSUnsubscribe(subscribe)
    NettyWSClientBuilder[IO].withIdleTimeout(5.seconds).resource.use {
      _.connect(WSRequest(wsUri)).use {
        (wsConnection: WSConnection[IO]) =>
          IO.println("Using wsConnection ...")
            >> IO.println(s"Subscribing ${subscribe.asFrame.toString} ...")
            >> wsConnection.send(subscribe.asFrame)
            >> wsConnection.receive.map("First frame: "+_.toString).map(IO.println)
            >> wsConnection.receiveStream.evalMap(upsertFrame).evalMap(_ => killSwitch.get).takeWhile(_==false).compile.drain
            >> wsConnection.send(unsubscribe.asFrame)
            >> IO.println("End wsConnection")
      } >> IO.println("WSClient close")
    }
  end websocketUpdate

  private def splitTableStatuses(l: List[CandleTableStatus]): Try[(CandleTableStatus, List[CandleTableStatus])] = Try {
    val (other, latestGap) = l.span { s => !(s.ts2.isEmpty && s.duration.isEmpty) }
    assert(latestGap.length == 1)
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
      _ <- IO.println(s"Starting downloadAndSave(start=$startTime, end=$endTime, candleType=$candleType)")
      _ <- createCandleTable(candleType).transact(tx)
      candles <- getCandleStream(httpClient, GET_HISTORY_CANDLES)(startTime, endTime, candleType)
        // .evalTap { (c: List[Candle]) =>
        //   val m = s"Downloaded ${c.length} candles [${c.map(_.ts).min}, ${c.map(_.ts).max}], candleType=$candleType."
        //   IO.println(m)
        // }
        .evalMap { (cs: List[Candle]) => upsertCandle(candleType, cs).transact(tx) }
        .compile
        .toList
    } yield ()

  end downloadAndSave
