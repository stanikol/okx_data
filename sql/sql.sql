with D as (select ts,
                (lead(ts, 1) over w) as ts2,
                age((lead(ts, 1) over w), ts) as delta_interval,
                extract(epoch from age((lead(ts, 1) over w), ts)) as delta
            from spot_candle_BTC_USDT_1H
            window w as (order by ts asc)
            order by ts asc)
select * from D
where delta_interval <> interval '1 hour' or delta_interval is null
