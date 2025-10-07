package client

import candles._
import cats._
import cats.syntax.traverse._
import utils.DateTimeUtils.decodeDateTime

import scala.util.Try

case class GetCandlesResponse(
  code: Option[String],
  msg: Option[String],
  data: List[List[String]]
) {
  def toCandles: Try[List[Candle]] = data.traverse(convertToCandle)

  private def convertToCandle(xs: Seq[String]): Try[Candle] = for {
    _ <- Try(assert(xs.length >= 6, "Invalid candle data!"))
    ts <- decodeDateTime(xs.head)
    o <- parseBigDecimal(xs(1))
    h <- parseBigDecimal(xs(2))
    l <- parseBigDecimal(xs(3))
    c <- parseBigDecimal(xs(4))
    volume <- parseBigDecimal(xs(5))
    volCcy <- parseBigDecimal(xs(6))
    volCcyQuote <- parseBigDecimal(xs(7))
  } yield Candle(ts, o, h, l, c, volume, volCcy, volCcyQuote, xs(8))

  private def parseBigDecimal(s: String): Try[BigDecimal] = Try(BigDecimal(s))

}
