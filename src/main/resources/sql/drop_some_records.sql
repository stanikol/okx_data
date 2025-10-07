with candles as (
    select row_number(ts) over (order by ts asc), ts
    from table_name
    where
) delete from candles