import pandas as pd
import psycopg2 as psql
from datetime import datetime

UNIT_SIZE = 1e7


def load_data(
    table_name: str, startTs, endTs: datetime, unit_size=UNIT_SIZE
) -> pd.DataFrame:
    connection = psql.connect(
        database="okx", user="okx", password="example", host="127.0.0.1", port=5432
    )
    RENAME_COLS = {
        "open": "Open",
        "high": "High",
        "low": "Low",
        "close": "Close",
        "volume": "Volume",
    }
    _startTs: str = startTs.isoformat()
    _endTs: str = endTs.isoformat()
    sql = \
        f"""select * from spot_candle_btc_usdt_1h
            where ts >= '${_startTs}'::timestamp and ts <= '${_endTs}'::timestamp ;"""
    df = pd.read_sql(sql, connection).rename(columns=RENAME_COLS)
    df.ts = pd.to_datetime(df.ts)
    df.set_index(["ts"], inplace=True)
    df2 = df[["Open", "High", "Low", "Close"]] / unit_size
    df2["Volume"] = df.Volume * unit_size
    return df2
