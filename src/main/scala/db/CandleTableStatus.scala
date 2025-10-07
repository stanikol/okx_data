package db

import java.time.LocalDateTime
import scala.concurrent.duration.Duration

case class CandleTableStatus(ts: LocalDateTime, ts2: Option[LocalDateTime], duration: Option[Duration])
