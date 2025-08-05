package candles
import doobie.Put
import doobie.util.Get

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class Candle(
    ts: LocalDateTime,
    o: BigDecimal,
    h: BigDecimal,
    l: BigDecimal,
    c: BigDecimal,
    s: String
)

object Candle {

  private val psqlDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")

//  given putLocalDateTime: Put[LocalDateTime] =
//    Put[Timestamp].tcontramap{(ldt: LocalDateTime) =>
//      val dt = ldt.format(psqlDateTimeFormatter)
//      println(s"dt=$dt")
//      Timestamp.valueOf(dt)
//    }

  given putLocalDateTime: Put[LocalDateTime] =
    Put[Timestamp].tcontramap{(ldt: LocalDateTime) =>
      Timestamp.valueOf(ldt)
    }

  given getLocalDateTime: Get[LocalDateTime] = Get[Timestamp].tmap(ts => ts.toLocalDateTime)
}
