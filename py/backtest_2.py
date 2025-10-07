import pandas as pd

from backtesting import Strategy
from backtesting.lib import crossover
from backtesting import Backtest
from load_data import load_data
from datetime import datetime


def bbands_lower(values, window, num_std):
    rolling = pd.Series(values).rolling(window)
    mean, std = rolling.mean(), rolling.std()
    lower_band = mean - (std * num_std)
    return lower_band


def bbands_upper(values, window, num_std):
    rolling = pd.Series(values).rolling(window)
    mean, std = rolling.mean(), rolling.std()
    upper_band = mean + (std * num_std)
    return upper_band


class BollingerBandsStrategy(Strategy):
    # Define the parameters for the Bollinger Bands
    window = 31
    num_std = 2
    pp = 9

    def init(self):
        # Calculate the Bollinger Bands
        self.lower_band = self.I(bbands_lower, self.data.Close, self.window, self.num_std)
        self.upper_band = self.I(bbands_upper, self.data.Close, self.window, self.num_std)

    def next(self):
        # Buy signal: price crosses below the lower band
        minimum = min(self.data.Close[-1], self.data.Low[-1])
        if minimum < self.lower_band[-1] and self.position.size == 0:
            self.buy()

        # Sell signal: price crosses above the upper band
        elif self.position.is_long and self.trades[0].pl_pct >= float(self.pp)/1000:
            self.position.close()


if __name__ == "__main__":
    startTs = datetime(2025, 1, 1)
    endTs = datetime(2025, 8, 1)
    df = load_data("spot_candle_btc_usdt_1h", startTs, endTs, 100000)
    bt = Backtest(df, BollingerBandsStrategy, cash=1_000, commission=0.002, trade_on_close=True)
    # stats = bt.optimize(
    #     n1=range(15, 20),
    #     n2=range(35, 50),
    #     pct=range(1, 20),
    #     # maximize="Equity Final [$]",
    #     constraint=lambda param: param.n1 < param.n2,
    # )
    stats = bt.run()
    print(stats)
    print(stats._strategy)
    stats._trades.to_csv("trades.tsv", sep="\t")
