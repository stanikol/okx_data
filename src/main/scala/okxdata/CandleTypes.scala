package okxdata
import candles.*
 
object CandleTypes:
  val candleTypes =  List(CandleType((Currency.BTC, Currency.USDT), CandleSize.`1m`),
                         CandleType((Currency.BTC, Currency.USDT), CandleSize.`1H`),
                         CandleType((Currency.BTC, Currency.USDT), CandleSize.`1Dutc`),
                         CandleType((Currency.XRP, Currency.USDT), CandleSize.`1m`),
                         CandleType((Currency.XRP, Currency.USDT), CandleSize.`1H`),
                         CandleType((Currency.XRP, Currency.USDT), CandleSize.`1Dutc`))
end CandleTypes
