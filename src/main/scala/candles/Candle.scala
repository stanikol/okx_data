package candles

import utils.DateTimeUtils.decodeDateTime

import java.time.LocalDateTime
import java.time.{Duration => DurationJava}
import scala.concurrent.duration._
import scala.jdk.DurationConverters._
import scala.util.Try

enum CandleSize(val duration: FiniteDuration):
  case `1m` extends CandleSize(1.minute)
  case `3m` extends CandleSize(2.minute)
  case `5m` extends CandleSize(5.minutes)
  case `15m` extends CandleSize(15.minutes)
  case `30m` extends CandleSize(30.minutes)
  case `1H` extends CandleSize(1.hour)
  case `2H` extends CandleSize(2.hour)
  case `4H` extends CandleSize(4.hour)
  // UTC+0 opening price k-line:

  case `6Hutc` extends CandleSize(6.hour)
  case `12Hutc` extends CandleSize(12.hour)
  case `1Dutc` extends CandleSize(1.day)
  case `1Wutc` extends CandleSize(7.days)
  case `1Mutc` extends CandleSize(31.days)
//  case `6H`, `12H`, `1D`, `1W`, `1M` // UTC+8 opening price k-line

object CandleSize:
  def apply(s: String): Try[CandleSize] = Try {
    CandleSize.valueOf(s)
  }

  def countTicks(startTime: LocalDateTime, endTime: LocalDateTime, candleSize: CandleSize): Long =
    val timeSpan: FiniteDuration = DurationJava.between(startTime, endTime).toScala
    val count: Long = timeSpan.toNanos / candleSize.duration.toNanos
    count
  end countTicks

end CandleSize

enum Currency:
  case USDT, BTC, XRP
end Currency

type Pair = (Currency, Currency)

case class CandleType(pair: Pair, candleSize: CandleSize) {
  val candleTableName: String = s"spot_candle_${pair._1}_${pair._2}_${candleSize}"

}

case class Candle(
  ts: LocalDateTime,
  open: BigDecimal,
  high: BigDecimal,
  low: BigDecimal,
  close: BigDecimal,
  volume: BigDecimal,
  volCcy: BigDecimal,
  volCcyQuote: BigDecimal,
  confirm: String
)

object Candle:
  // https://my.okx.com/docs-v5/en/#order-book-trading-market-data-get-candlesticks-history
  def fromStrings(xs: Seq[String]): Try[Candle] = {
    for {
      _ <- Try(assert(xs.length == 9, s"Invalid candle data! Can not parse candle from this seq: ${xs.mkString("[",", ","]")}, len=${xs.length}"))
      ts <- decodeDateTime(xs.head)
      o <- parseBigDecimal(xs(1))
      h <- parseBigDecimal(xs(2))
      l <- parseBigDecimal(xs(3))
      c <- parseBigDecimal(xs(4))
      volume <- parseBigDecimal(xs(5))
      volCcy <- parseBigDecimal(xs(6))
      volCcyQuote <- parseBigDecimal(xs(7))
    } yield Candle(ts, o, h, l, c, volume, volCcy, volCcyQuote, xs(8))
  }

  def fromStringsBrief(xs: Seq[String]): Try[Candle] = {
    for {
      _ <- Try(assert(xs.length == 6, s"Invalid candle data: xs.length=${xs.length} != 6 !"))
      ts <- decodeDateTime(xs.head)
      o <- parseBigDecimal(xs(1))
      h <- parseBigDecimal(xs(2))
      l <- parseBigDecimal(xs(3))
      c <- parseBigDecimal(xs(4))
    } yield Candle(ts, o, h, l, c, 0, 0, 0, xs(5))
  }
//    .recoverWith { case e => println(s"Error in Candle.fromStrings($xs): ${e.getMessage} !"); Failure(e) }

  private def parseBigDecimal(s: String): Try[BigDecimal] = Try(BigDecimal(s))

end Candle
