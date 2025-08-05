package candles

enum CandleSize:
  case `1m`, `3m`, `5m`, `15m`, `30m`, `1H`, `2H`,`4H`
  case `6H`, `12H`, `1D`, `1W`, `1M` // UTC+8 opening price k-line
  case `6Hutc`, `12Hutc`, `1Dutc`, `1Wutc`, `1Mutc` // UTC+0 opening price k-line:
