with D as (select ts,
        (lead(ts, 1) over w) as ts2,
        age((lead(ts, 1) over w), ts) as delta_interval,
        extract(epoch from age((lead(ts, 1) over w), ts)) as duration
    from spot_candle_btc_usdt_1m
    where confirm = '1'
    window w as (order by ts asc)
    order by ts asc)
  select * from D
    where delta_interval <> '1 minute' ::interval or delta_interval is null ;
