
package example

import org.scalatest._

import java.sql._
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone

import flatspec._
import matchers._

class Tst extends AnyFlatSpec with should.Matchers {
  // val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
  "Some damm test" should "work" in {
    java.util.TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")))
    val ldt1: LocalDateTime = LocalDateTime.parse("2025-03-09T01:00")
    val ldt2: LocalDateTime = LocalDateTime.parse("2025-03-09T02:00")
    val ldt3: LocalDateTime = LocalDateTime.parse("2025-03-09T03:00")
    val lst = List(ldt1, ldt2, ldt3)
    val test1 = lst.map(ldt => ldt -> Timestamp.valueOf(ldt))
    println(s"test1=$test1")

    val getInstat = {(ldt: LocalDateTime) => ldt.atZone(ZoneId.of("UTC")).toInstant}
    val test2 = lst.map(ldt => (ldt, getInstat(ldt)) ).map{
        case (ldt, inst) => (ldt, Timestamp.from(inst), Timestamp.valueOf(ldt))
      }
    println(s"test2=$test2")
    // val ts = Timestamp.valueOf(ldt)
    // val ts2 = new Timestamp(millis)
    // println(ldt.toString + "  "+ ts.toString() +"  "+ ts2.toString)
    // val ts3 = Timestamp.valueOf("2025-03-09 03:00:00")
    // println(s"ts3=$ts3")
  }

}
  
