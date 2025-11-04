package man

import candles.CandleType
import org.http4s.client.websocket.WSFrame.Text

case class WSSubscription(candleType: CandleType) {
  val id = candleType.candleTableName.replace("_", "").take(32)
  val channel = s"index-candle${candleType.candleSize}"
  val instId = candleType.pair._1.toString() + "-" + candleType.pair._2
  val subscribe = Text(s"""{"id":"$id", "op":"subscribe", "args": [{"channel": "$channel", "instId": "$instId"}]}""")
  val unsubscribe = Text(s"""{"id":"$id", "op":"unsubscribe"}""")
}
