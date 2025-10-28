package db
import doobie.util.Get

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

object Utils {
  given convertDateTimeFromLong: Get[Duration] = Get[Long].tmap(l => Duration(l, TimeUnit.MILLISECONDS))
  // given convertDateTimeFromStr: Get[Duration] = Get[String].tmap { strToDuration }

  // def strToDuration(s: String): Duration = {
  //   println(s"strToDuration($s)") //todo
  //   val HoursRegex: Regex = """(\d+):(\d+):(\d+)""".r
  //   val DaysRegex: Regex = """(\d+\s+[a-z]+)""".r
  //   val days: Duration = DaysRegex.findAllIn(s).map { s =>
  //     println(s"days=$s"); s //todo
  //   }.map(Duration.apply).foldLeft(Duration("0 days"))(_ + _)
  //   val r: Option[String] = HoursRegex.findAllIn(s).toList.headOption

  //   r.map { case hours =>
  //     hours match {
  //       case HoursRegex(hh, mm, ss) =>
  //         List(hh, mm, ss).zip(List("hours", "minutes", "seconds"))
  //           .map((d, tu) => Duration(s"$d $tu")).foldLeft(days)(_ + _)
  //     }
  //   }.getOrElse(days)

  // }
}
