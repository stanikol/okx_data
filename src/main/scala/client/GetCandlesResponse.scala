package client

import candles.{Candle, CandleSize, Pair}
import cats.*
import cats.effect.*
import cats.syntax.traverse.*
import doobie.ConnectionIO
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.http4s.client.middleware.FollowRedirect
import org.http4s.implicits.{path, uri}
import org.http4s.netty.client.NettyClientBuilder
import utils.DateTimeUtils.{decodeDateTime, encodeDateTime}

import java.time.LocalDateTime
import scala.concurrent.duration.*
import scala.util.Try

case class GetCandlesResponse(
    code: Option[String],
    msg: Option[String],
    data: List[List[String]]
) {
  def toCandles: Try[List[Candle]] = data.traverse(convertToCandle)

  private def convertToCandle(xs: Seq[String]): Try[Candle] = for {
    _ <- Try(assert(xs.length == 6, "Invalid candle data!"))
    ts <- decodeDateTime(xs.head)
    o <- parseBigDecimal(xs(1))
    h <- parseBigDecimal(xs(2))
    l <- parseBigDecimal(xs(3))
    c <- parseBigDecimal(xs(4))
  } yield Candle(ts, o, h, l, c, xs(5))

  private def parseBigDecimal(s: String): Try[BigDecimal] = Try(BigDecimal(s))

}
