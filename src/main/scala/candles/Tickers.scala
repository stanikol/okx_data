package candles
import java.util.EnumMap

enum Tickers:
  case USDT, BTC

type Pair = (Tickers, Tickers)