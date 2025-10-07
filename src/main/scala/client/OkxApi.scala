package db

import cats.effect._

import java.time.LocalDateTime
import java.time.ZoneId

trait OkxApi:

  def getCurrentTimeOnOkx: IO[LocalDateTime] = IO(LocalDateTime.now(ZoneId.of("UTC")))
