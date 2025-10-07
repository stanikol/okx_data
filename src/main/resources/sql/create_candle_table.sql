create table if not exists table_name (
    ts TIMESTAMP primary key,
    open DECIMAL not null,
    high DECIMAL not null,
    low DECIMAL not null,
    close DECIMAL not null,
    volume          DECIMAL not null,
    volCcy          DECIMAL not null,
    volCcyQuote     DECIMAL not null,
    confirm         varchar
);