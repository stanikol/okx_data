
import pandas as pd
import psycopg2 as psql

from backtesting import Strategy
from backtesting.lib import crossover
from backtesting import Backtest


class SmaCross(Strategy):
    # Define the two MA lags as *class variables*
    # for later optimization
    n1 = 10
    n2 = 20

    def init(self):
        # Precompute the two moving averages
        self.sma1 = self.I(SMA, self.data.Close, self.n1)
        self.sma2 = self.I(SMA, self.data.Close, self.n2)

    def next(self):
        # If sma1 crosses above sma2, close any existing
        # short trades, and buy the asset
        if crossover(self.sma1, self.sma2):
            self.position.close()
            self.buy()

        # Else, if sma1 crosses below sma2, close any existing
        # long trades, and sell the asset
        elif crossover(self.sma2, self.sma1):
            self.position.close()
            self.sell()







if __name__ == '__main__':
    connection = psql.connect(database='okx', user='okx', password='example',
                              host='127.0.0.1', port=5432)
    RENAME_COLS = {'open': 'Open', 'high': 'High', 'low': 'Low', 'close': 'Close', 'volume': 'Volume'}
    df = pd.read_sql("select * from okx_candle_btc_usdt_1m;", connection)\
            .rename(columns=RENAME_COLS)
    df = (df / 1e6).assign(Volume=df.Volume * 1e6)
    df.ts = df.to_datetime(df.ts)
    print(df.head)
    

    bt = Backtest(df, SmaCross, cash=10_000, commission=.002)
    stats = bt.run()
    print(stats)
