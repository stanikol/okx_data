package db
import candles.Candle
import doobie.*
import doobie.implicits.*

import java.time.LocalDateTime
import scala.io.Source

trait CandleTable {
  import Candle.given

  private val sqlCreateCandleTable: String =
    Source.fromResource("sql/create_candle_table.sql").mkString
  private val sqlInsertCandle: String =
    Source.fromResource("sql/insert_candle.sql").mkString
  private val sqlSelectCandles: String =
    Source.fromResource("sql/select_candles.sql").mkString

  def createCandleTable(tableName: String): ConnectionIO[Int] =
    Fragment
      .const0(sqlCreateCandleTable.replace("table_name", tableName))
      .update
      .run


  def insertCandles(
      tableName: String,
      candles: List[Candle]
  ): ConnectionIO[Int] = {
    val sql = Update[Candle](sqlInsertCandle.replace("table_name", tableName))
    sql.updateMany(candles.sortBy(_.ts))
  }

  def selectCandles(tableName: String): ConnectionIO[List[Candle]] =
    val sql = sqlSelectCandles.replace("table_name", tableName)
    Query0.apply[Candle](sql).to[List]
}
