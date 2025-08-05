package db

import cats.effect.{IO, Resource}
import conf.DbConf
import doobie.*
import doobie.util.log.LogEvent
import org.http4s.client.Client
import org.http4s.client.middleware.FollowRedirect
import org.http4s.netty.client.NettyClientBuilder

import scala.concurrent.duration.*

trait DoobieTransactor {

  def httpClient: Resource[IO, Client[IO]] =
    NettyClientBuilder[IO]
      .withIdleTimeout(10.seconds)
      .resource
      .map(FollowRedirect(5))

  private val printSqlLogHandler: LogHandler[IO] = new LogHandler[IO] {
    def run(logEvent: LogEvent): IO[Unit] = IO.println(
      "SQL: " + logEvent.sql + "\n\tsql_params: " + logEvent.params.allParams.mkString
    )
  }

  private def acquire(c: DbConf): IO[Transactor[IO]] = IO {
    import c.*
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = s"jdbc:postgresql://$host:$port/$dbname",
      user = user,
      password = pswd,
      logHandler = None // Some(printSqlLogHandler)
    )
  }

  def transactor(c: DbConf): Resource[IO, Transactor[IO]] =
    Resource.make(acquire(c)) { _ => IO.pure(()) }
}
