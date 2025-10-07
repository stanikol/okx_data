import pandas as pd

from backtesting import Strategy
from backtesting.lib import crossover
from backtesting import Backtest
from load_data import load_data
from datetime import datetime


def SMA(values, n):
    """
    Return simple moving average of `values`, at
    each step taking into account `n` previous values.
    """
    return pd.Series(values).rolling(n).mean()


class SmaCross(Strategy):
    # Define the two MA lags as *class variables*
    # for later optimization
    n1 = 15
    n2 = 45
    pct = 10

    def init(self):
        # Precompute the two moving averages
        self.sma1 = self.I(SMA, self.data.Close, self.n1)
        self.sma2 = self.I(SMA, self.data.Close, self.n2)
        self.latestBuyPrice = None
        self.show = 0

    def next(self):
        closePrice = self.data.Close[-1]
        # If sma1 crosses above sma2, close any existing
        # short trades, and buy the asset
        if (
            crossover(self.sma1, self.sma2)
            and self.latestBuyPrice is not None
            and self.latestBuyPrice * float(100+self.pct)/100 <= closePrice
        ):
            self.position.close()
            self.latestBuyPrice = None
        # Else, if sma1 crosses below sma2, close any existing
        # long trades, and sell the asset
        elif crossover(self.sma2, self.sma1) and self.position.size == 0:
            self.latestBuyPrice = closePrice
            self.buy()


if __name__ == "__main__":
    startTs = datetime(2024, 1, 1)
    endTs = datetime(2025, 1, 1)
    df = load_data("spot_candle_btc_usdt_1h", startTs, endTs, 100000)
    bt = Backtest(df, SmaCross, cash=500, commission=0.002, trade_on_close=True, finalize_trades=True)
    stats = bt.optimize(
        n1=range(15, 20),
        n2=range(35, 50),
        pct=range(1, 20),
        # maximize="Equity Final [$]",
        constraint=lambda param: param.n1 < param.n2,
    )
    # stats = bt.run()
    print(stats)
    print(stats._strategy.n1, stats._strategy.n2)
    stats._trades.to_csv("trades.tsv", sep="\t")
