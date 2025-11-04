package man


case class Arg(cannel: String, instId: String)

case class WSCandleFrame(
  arg: Arg,
  data: List[List[String]]
)

// psertFrame: Got WSFRAME= Text({"id":"spotcandleBTCUSDT1m","event":"subscribe","arg":{"channel":"index-candle1m","instId":"BTC-USDT"},"connId":"69ac8542"},true)
// Error DecodingFailure at .data: Missing required field
// upsertFrame(Text({"id":"spotcandleBTCUSDT1m","event":"subscribe","arg":{"channel":"index-candle1m","instId":"BTC-USDT"},"connId":"69ac8542"},true)): Finished
// upsertFrame: Got WSFRAME= Text({"arg":{"channel":"index-candle1m","instId":"BTC-USDT"},"data":[["1761824220000","110015.9","110016","109963.7","109963.7","0"]]},true)
// upsertFrame(Text({"arg":{"channel":"index-candle1m","instId":"BTC-USDT"},"data":[["1761824220000","110015.9","110016","109963.7","109963.7","0"]]},true)): Finished
// upsertFrame: Got WSFRAME= Text({"id":"spotcandleXRPUSDT1m","event":"subscribe","arg":{"channel":"index-candle1m","instId":"XRP-USDT"},"connId":"69ac8542"},true)
// Error DecodingFailure at .data: Missing required field
// upsertFrame(Text({"id":"spotcandleXRPUSDT1m","event":"subscribe","arg":{"channel":"index-candle1m","instId":"XRP-USDT"},"connId":"69ac8542"},true)): Finished
// upsertFrame: Got WSFRAME= Text({"arg":{"channel":"index-candle1m","instId":"XRP-USDT"},"data":[["1761824220000","2.5515","2.5519","2.5499","2.5499","0"]]},true)
// upsertFrame(Text({"arg":{"channel":"index-candle1m","instId":"XRP-USDT"},"data":[["1761824220000","2.5515","2.5519","2.5499","2.5499","0"]]},true)): Finished
