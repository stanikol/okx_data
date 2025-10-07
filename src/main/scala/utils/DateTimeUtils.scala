package utils
import java.time.{Instant, LocalDateTime, ZoneId}
import scala.util.Try

object DateTimeUtils {
  def decodeDateTime(s: String): Try[LocalDateTime] = Try {
    LocalDateTime.ofInstant(Instant.ofEpochMilli(s.toLong), ZoneId.of("UTC"))
  }

  def encodeDateTime(ts: LocalDateTime): String =
    ts.atZone(ZoneId.of("UTC")).toInstant.toEpochMilli.toString
}
