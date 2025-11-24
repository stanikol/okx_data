package client

import candles._
import cats._
import cats.effect._
import cats.syntax.all._
import io.circe._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.Client
import org.http4s.implicits.uri
import utils.DateTimeUtils.encodeDateTime

import java.time.LocalDateTime
import java.time.{Duration => DurationJava}
import scala.concurrent.duration._
import scala.jdk.DurationConverters._
import scala.math.Ordered.orderingToOrdered

trait OkxApiCandle {

  protected val GET_HISTORY_CANDLES: Uri = uri"https://okx.com/api/v5/market/history-candles"
//  protected val GET_HISTORY_CANDLES: Uri = uri"https://okx.com/api/v5/market/history-index-candles"

  protected val GET_CANDLES: Uri = uri"https://okx.com/api/v5/market/candles"
//  protected val GET_CANDLES: Uri = uri"https://okx.com/api/v5/market/index-candles"

  /** @param after
    *   \- END of period
    * @param before
    *   \- START of period
    */
  def getCandles(client: Client[IO], uriApi: Uri)(
    before: Option[LocalDateTime],
    after: Option[LocalDateTime],
    pair: Pair | String,
    candleSize: CandleSize | String,
    limit: Int = 1000
  ): IO[List[Candle]] = {
    val instId = pair match {
      case (x, y)    => s"$x-$y"
      case s: String => s
    }
    val bar = candleSize match {
      case cs: CandleSize => cs.toString
      case s: String      => s
    }
    val params = Map(
      "instId" -> instId,
      "bar" -> bar,
      "limit" -> limit.toString
    ) ++ after.map(ts => "after" -> encodeDateTime(ts))
      ++ before.map(ts => "before" -> encodeDateTime(ts))
    val uri: Uri = uriApi.withQueryParams(params)
    for {
      json: Json <- client.expect[Json](uri)
      response: GetCandlesResponse <- IO.fromEither(json.as[GetCandlesResponse])
      candles: List[Candle] <- IO.fromTry(response.toCandles)
    } yield candles
  }

  def getCandleStream(
    client: Client[IO],
    apiUri: Uri
  )(startTime: LocalDateTime, endTime: LocalDateTime, candleType: CandleType): fs2.Stream[IO, List[Candle]] = {
    fs2.Stream.unfoldEval((startTime, endTime)) {
      case (startTime, endTime) if startTime >= endTime => IO.pure(Option.empty)
      case (startTime, endTime) =>
        val candleDuration: FiniteDuration = candleType.candleSize.duration
        val duration: FiniteDuration = DurationJava.between(startTime, endTime).toScala
        val (startTime_, endTime_) = if (duration <= candleDuration)
          (Some(startTime.minus((candleDuration * 2).toJava)), Some(endTime.plus((candleDuration * 2).toJava)))
        else
          (Some(startTime), Some(endTime))
        for {
          candles <-
            getCandles(client, apiUri)(startTime_, endTime_, candleType.pair, candleType.candleSize, limit = 10000)
          next <- if (candles.nonEmpty) {
            // endTimepoint gives data in reverse order, like n-rows from the latest (endTime) parameter. From endTime to startTime to endTime.
            val (minTs, maxTs) = candles.map(_.ts).min -> candles.map(_.ts).max
            val (newstartTime, newendTime) = startTime -> List(minTs, endTime).min
            Option.when(newstartTime <= newendTime)(candles, newstartTime -> newendTime).pure[IO]
          } else {
            val m = s"Empty response in getCandleStream([$startTime, $endTime], $candleType, apiUri=$apiUri)!"
            IO.println(m) >> Option.empty.pure[IO]
          }
        } yield next
    }.evalTap { (cs: List[Candle]) =>
      val check: CheckCandleResult = detectErrors(cs, candleType.candleSize)
      val tss: List[LocalDateTime] = cs.map(_.ts)
      val (startts: LocalDateTime, endts: LocalDateTime) = tss.min -> tss.max
      val errorMsg: String =
        List(
          check.missingCandles.nonEmpty -> s"ERROR: Missing ticks detected [$startts, $endts]: ${check.missingCandles.mkString("\n")}!",
          check.duplicatedCandles.nonEmpty -> s"ERROR: Duplicated candles detected [$startts, $endts]: ${check.duplicatedCandles.mkString("\n")}"
        )
          .filter(_._1).map(_._2).mkString("\n")
      val msg = if (errorMsg.isEmpty())
        s"Recieved Candles: time=[$startts, $endts], count=${tss.length}, $candleType."
      else errorMsg
      IO.println(msg)
    }
  }

  private def detectErrors(cs: List[Candle], candleSize: CandleSize): CheckCandleResult = {
    val timestamps = cs.map(_.ts).sorted
    val missingCandles: List[MissingCandles] = timestamps.init.zip(timestamps.tail).map { (ts0, ts1) =>
      (startTime = ts0, endTime = ts1, duration = DurationJava.between(ts0, ts1).toScala)
    }.filter(_.duration != candleSize.duration)
    val duplicatedCandles: Map[LocalDateTime, Int] =
      timestamps.groupBy(identity).mapValues(_.length).filter(_._2 > 1).toMap
    (missingCandles, duplicatedCandles)
  }
}

type MissingCandles = (startTime: LocalDateTime, endTime: LocalDateTime, duration: FiniteDuration)

type CheckCandleResult = (missingCandles: List[MissingCandles], duplicatedCandles: Map[LocalDateTime, Int])
