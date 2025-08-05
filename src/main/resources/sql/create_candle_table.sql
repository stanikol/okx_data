create table if not exists table_name (
    ts TIMESTAMP primary key,
    o DECIMAL not null,
    h DECIMAL not null,
    l DECIMAL not null,
    c DECIMAL not null,
    s VARCHAR
);