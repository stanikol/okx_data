insert into table_name
values (?, ?, ?, ?, ?, ?)
on conflict (ts) do update set
        o = EXCLUDED.o,
        h = EXCLUDED.h,
        l = EXCLUDED.l,
        c = EXCLUDED.c,
        s = EXCLUDED.s
    where table_name.s = '0' and EXCLUDED.s='1';
