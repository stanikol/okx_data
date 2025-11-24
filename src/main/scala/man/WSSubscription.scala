package man

import candles.CandleType
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.client.websocket.WSFrame.Text

import java.util.UUID


case class Arg(channel: String, instId: String)

case class WSCandleFrame(
  arg: Arg,
  data: List[List[String]]
)

case class WSSubscribe(id: String, args: List[Arg], op:String="subscribe"){
  def asFrame: Text = Text(this.asJson.noSpaces)
}

object WSSubscribe {
  def apply(ct: List[CandleType]): WSSubscribe = {
    val id = UUID.randomUUID.toString.replace("-", "").take(32)
    val args = ct.map(ct => Arg(s"index-candle${ct.candleSize}", ct.pair._1.toString + "-" + ct.pair._2))
    WSSubscribe(id, args)
  }
}

case class WSUnsubscribe(id: String, args: List[Arg], op: String ="unsubscribe"){
  def asFrame: Text = Text(this.asJson.noSpaces)
}

object WSUnsubscribe {
  def apply(subscribe: WSSubscribe): WSUnsubscribe = WSUnsubscribe(subscribe.id, subscribe.args)
}

