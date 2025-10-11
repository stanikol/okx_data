package example

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
// import concurrent.duration.*

object Test2 extends IOApp:
  override def run(args: List[String]): IO[ExitCode] = IO {
    while (true) {
      println("Hello")
      Thread.sleep(10000L)
    }
    ExitCode.Success
  }
