package man

import candles.CandleType
import org.http4s.client.websocket.WSFrame.Text
import io.circe.*, io.circe.syntax.*, io.circe.generic.auto.*
import java.util.UUID


case class Arg(channel: String, instId: String)

case class WSCandleFrame(
  arg: Arg,
  data: List[List[String]]
)

case class WSSubscribe(id: String, args: List[Arg], op:String="subscribe"){
  def asFrame: Text = Text(this.asJson.toString)
}

object WSSubscribe {
  def apply(ct: List[CandleType]): WSSubscribe = {
    val id = UUID.randomUUID.toString.replace("-", "").take(32)
    val args = ct.map(ct => Arg(s"index-candle${ct.candleSize}", ct.pair._1.toString() + "-" + ct.pair._2))
    WSSubscribe(id, args)
  }
}

case class WSUnsubscribe(id: String, op: String ="unsubscribe"){
  def asFrame: Text = Text(this.asJson.toString)
}

object WSUnsubscribe {
  def apply(subscribe: WSSubscribe): WSUnsubscribe = WSUnsubscribe(subscribe.id)
}


// case class WSSubscription(candleTypes: List[CandleType]):
//   import WSSubscription.*
//   private val id = UUID.randomUUID.toString.replace("-", "").take(32)
//   private val args = candleTypes.map(ct => s"""{"channel": "${channel(ct)}", "instId": "${instId(ct)}"}""").mkString("[", ", ", "]")
//   val subscribe = Text(s"""{"id":"$id", "op":"subscribe", "args": $args}""")
//   val unsubscribe = Text(s"""{"id":"$id", "op":"unsubscribe"}""")
// end WSSubscription

// object WSSubscription:
//   def channel(ct: CandleType) = s"index-candle${ct.candleSize}"
//   def instId(ct: CandleType) = ct.pair._1.toString() + "-" + ct.pair._2
// end WSSubscription
