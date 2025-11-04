package db

import cats.effect.IO
import cats.effect.kernel.Resource
import conf.ApplicationConf
import conf.DbConf
import doobie._
import doobie.util.log.LogEvent
import io.circe.config.parser
import io.circe.generic.auto._
trait DoobieTransactor {

  val transactor: Resource[IO, Transactor[IO]] = for {
    applicationConf <- Resource.liftK.apply(parser.decodeF[IO, ApplicationConf]())
    r <- Resource.make(acquire(applicationConf.db))(_ => IO.unit)
  } yield r

  private def acquire(db: DbConf) = IO(
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = s"jdbc:postgresql://${db.host}:${db.port}/${db.dbname}", // Connect URL
      user = db.user,
      password = db.pswd,
      logHandler = None
      // Some(LogHandler.jdkLogHandler)
      // Some(printSqlLogHandler)
    )
  )

  private val printSqlLogHandler: LogHandler[IO] = new LogHandler[IO] {
    def run(logEvent: LogEvent): IO[Unit] =
      IO {
        // println(s"SQL QUERY: " + logEvent.sql + "\nSQL PARAMS: " + logEvent.params.allParams)
        println("SQL QUERY: " + logEvent.sql + "\nSQL PARAMS COUNT: " + logEvent.params.allParams.length)
      }
  }
}
