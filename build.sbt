
val http4sVersion = "1.0.0-M44" //"0.23.30"

ThisBuild / scalaVersion := "3.3.4"

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
// https://mvnrepository.com/artifact/org.http4s/http4s-netty-client
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