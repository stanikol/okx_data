import candles._
import org.scalatest._

import java.time.LocalDateTime

import CandleSize._
import flatspec._
import matchers._

class CandleTest extends AnyFlatSpec with should.Matchers {

  "CandleSize.countTicks" should "work" in {
    val ts2: LocalDateTime = LocalDateTime.now()
    val ts: LocalDateTime = ts2.minusHours(1).minusMinutes(1)
    countTicks(ts, ts2, `1m`) should be(61L)
    countTicks(ts2, ts, `1m`) should be(-61L)

  }

}
