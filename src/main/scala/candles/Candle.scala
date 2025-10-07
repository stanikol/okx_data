package candles

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

  def countTicks(startTime: LocalDateTime, endTime: LocalDateTime, candelSize: CandleSize): Long =
    val timeSpan: FiniteDuration = DurationJava.between(startTime, endTime).toScala
    val count: Long = timeSpan.toNanos / candelSize.duration.toNanos
    count
  end countTicks

end CandleSize

enum Currency:
  case USDT, BTC
end Currency

type Pair = (Currency, Currency)

type CandleType = (pair: Pair, candleSize: CandleSize)

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
