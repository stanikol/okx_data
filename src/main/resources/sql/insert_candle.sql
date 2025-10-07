insert into table_name
values (?, ?, ?, ?, ?, ?, ?, ?, ?)
on conflict (ts) do update set
        open        = EXCLUDED.open,
        high        = EXCLUDED.high,
        low         = EXCLUDED.low,
        close       = EXCLUDED.close,
        volume      = EXCLUDED.volume,
        volCcy      = EXCLUDED.volCcy,
        volCcyQuote = EXCLUDED.volCcyQuote,
        confirm     = EXCLUDED.confirm
    where table_name.confirm = '0' and EXCLUDED.confirm = '1';
