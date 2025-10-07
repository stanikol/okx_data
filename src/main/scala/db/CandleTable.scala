package db
import candles._
import doobie._
import doobie.implicits._
import doobie.implicits.javatimedrivernative.JavaLocalDateTimeMeta

import scala.io.Source

trait CandleTable {
  import db.Utils.given

  private val sqlCreateCandleTable: String =
    Source.fromResource("sql/create_candle_table.sql").mkString
  private val sqlUpsertCandle: String =
    Source.fromResource("sql/insert_candle.sql").mkString
  private val sqlSelectCandles: String =
    Source.fromResource("sql/select_candles.sql").mkString
  private val sqlGetCandleTableStatus: String =
    Source.fromResource("sql/get_candle_table_status.sql").mkString

  def createCandleTable(candleType: CandleType): ConnectionIO[Int] =
    createCandleTable(candleTableName(candleType))

  def createCandleTable(tableName: String): ConnectionIO[Int] =
    Fragment
      .const0(sqlCreateCandleTable.replace("table_name", tableName))
      .update
      .run

  def upsertCandle(candleType: CandleType, data: List[Candle]): ConnectionIO[Int] =
    upsertCandle(candleTableName(candleType), data)

  def upsertCandle(tableName: String, candles: List[Candle]): ConnectionIO[Int] = {
    val sql = Update[Candle](sqlUpsertCandle.replace("table_name", tableName))
    val update = sql.updateMany(candles.sortBy(_.ts))
    update
  }

  def selectCandles(candleType: CandleType): ConnectionIO[List[Candle]] =
    selectCandles(candleTableName(candleType))

  def selectCandles(tableName: String): ConnectionIO[List[Candle]] =
    val sql = sqlSelectCandles.replace("table_name", tableName)
    Query0.apply[Candle](sql).to[List]

  def getCandleTableStatus(candleType: CandleType): ConnectionIO[List[CandleTableStatus]] =
    val tableName = candleTableName(candleType)
    val duration = candleType.candleSize.duration.toString()
    val sql = sqlGetCandleTableStatus.replace("table_name", tableName)
    Query[String, CandleTableStatus](sql).to[List](duration)

  def candleTableName(ct: CandleType): String = s"spot_candle_${ct.pair._1}_${ct.pair._2}_${ct.candleSize}"

}
