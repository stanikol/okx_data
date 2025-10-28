
with D as (
    select ts,
           (lead(ts, 1) over w) as ts2,
           extract(epoch from age((lead(ts, 1) over w), ts)) as duration
    from table_name
    where confirm = '1'
    window w as (order by ts asc)
    order by ts asc
)
select * from D
where duration <> extract(epoch from ? ::interval) or duration is null ;
