package client

import candles._
import cats._
import cats.syntax.traverse._

import scala.util.Try

case class GetCandlesResponse(
  code: Option[String],
  msg: Option[String],
  data: List[List[String]]
) {
  def toCandles: Try[List[Candle]] = data.traverse(Candle.fromStrings)

}
