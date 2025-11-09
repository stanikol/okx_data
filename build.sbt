val http4sVersion = "1.0.0-M44" //"0.23.30"

ThisBuild / scalaVersion := "3.7.4"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalacOptions ++= Seq(
  "-Wunused:imports",
  // "-encoding",
  // "utf8",          // if an option takes an arg, supply it on the same line
  // "-Xlint",                     // exploit "trailing comma" syntax so you can add an option without editing this line
  // "-feature",                   // then put the next option on a new line for easy editing
  // "-language:implicitConversions",
  // "-language:existentials",
  // "-unchecked",
  "-Werror"
)

// https://mvnrepository.com/artifact/org.typelevel/cats-core
libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
// https://mvnrepository.com/artifact/org.typelevel/cats-effect
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.6.3"
// https://mvnrepository.com/artifact/io.circe/circe-core
libraryDependencies += "io.circe" %% "circe-core" % "0.14.14"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-circe" % http4sVersion,
  // Optional for auto-derivation of JSON codecs
  "io.circe" %% "circe-generic" % "0.14.14",
  // Optional for string interpolation to JSON model
  "io.circe" %% "circe-literal" % "0.14.14"
)
// https://mvnrepository.com/artifact/org.http4s/http4s-core
libraryDependencies += "org.http4s" %% "http4s-core" % "1.0.0-M44"
// https://mvnrepository.com/artifact/org.http4s/http4s-netty-client
libraryDependencies += "org.http4s" %% "http4s-netty-server" % "1.0.0-M2"
libraryDependencies += "org.http4s" %% "http4s-netty-client" % "1.0.0-M2"
// https://mvnrepository.com/artifact/org.tpolecat/doobie-core
libraryDependencies += "org.tpolecat" %% "doobie-core" % "1.0.0-RC10"
// https://mvnrepository.com/artifact/org.tpolecat/doobie-postgres
libraryDependencies += "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC10"
// available for 2.12, 2.13, 3.2
libraryDependencies += "co.fs2" %% "fs2-core" % "3.12.0"
// optional I/O library
libraryDependencies += "co.fs2" %% "fs2-io" % "3.12.0"
// https://mvnrepository.com/artifact/io.circe/circe-config
libraryDependencies += "io.circe" %% "circe-config" % "0.10.2"
//// https://mvnrepository.com/artifact/org.knowm.xchange/xchange-core
//libraryDependencies += "org.knowm.xchange" % "xchange-core" % "5.2.2"
//// https://mvnrepository.com/artifact/org.knowm.xchange/xchange-stream-core
//libraryDependencies += "org.knowm.xchange" % "xchange-stream-core" % "5.2.2"
//// https://mvnrepository.com/artifact/org.knowm.xchange/xchange-okex
//libraryDependencies += "org.knowm.xchange" % "xchange-okex" % "5.2.2"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.19"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

ThisBuild / assemblyMergeStrategy := {
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case x => MergeStrategy.first
    // val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    // oldStrategy(x)
}
