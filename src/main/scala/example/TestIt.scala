import cats._
import cats.effect._
import org.http4s.Uri
import org.http4s.client.websocket.WSFrame.Text
import org.http4s.client.websocket._
import org.http4s.netty.client._
import org.http4s.syntax.all._

import concurrent.duration._

object TestIt extends IOApp.Simple:
//  val wsUri = uri"wss://wspap.okx.com:8443/ws/v5/business"
  val wsUri: Uri = uri"wss://ws.okx.com:8443/ws/v5/business"
  val m1: Text = WSFrame.Text(
    """{"id":"snc1usdtbtc1m", "op":"subscribe", "args": [{"channel": "index-candle1m", "instId": "BTC-USD"}]}"""
  )
  val wsClient: Resource[IO, WSClient[IO]] = NettyWSClientBuilder[IO].withIdleTimeout(5.seconds).resource

  override def run: IO[Unit] =
    IO.println("Starting ...")
      >> wsClient.use { wsClient =>
        IO.println("Using client ...")
          >> wsClient.connect(WSRequest(wsUri)).use { (wsConnection: WSConnection[IO]) =>
            IO.println("Using  ...")
              >> wsConnection.send(m1) >> wsConnection.receiveStream.evalMap(IO.println).take(7).compile.drain
              >> wsConnection.send(Text("""{"id":"snc1usdtbtc1m", "op":"unsubscribe"}"""))
              >> IO.readLine >> IO.println("End connection")
          }
          >> IO.println("End client")
      }
      >> IO.println("End")
end TestIt
