import sbt.*

object Lib {
  object Version {
    val scala          = "3.3.0-RC3"
    val service        = "0.1.0-SNAPSHOT"
    val logbackClassic = "1.4.6"
    val slf4j          = "2.0.5"
    val scalaLogging   = "3.9.5"
    val config         = "1.4.2"
    val catsEffect     = "3.5.0-RC3"
    val circe          = "0.14.5"
    val fs2            = "3.7.0-RC4"
  }

  val sfl4j: List[ModuleID] = List(
    "org.slf4j" % "slf4j-nop" % Version.slf4j
  )

  val logback: List[ModuleID] = List(
    "ch.qos.logback" % "logback-classic" % Version.logbackClassic
  )

  val scalaLogging: List[ModuleID] = List(
    "com.typesafe.scala-logging" %% "scala-logging" % Version.scalaLogging
  )

  val config: List[ModuleID] = List(
    "com.typesafe" % "config" % Version.config
  )

  val circe: Seq[ModuleID] = List(
    "io.circe" %% "circe-core"    % Version.circe,
    "io.circe" %% "circe-generic" % Version.circe,
    "io.circe" %% "circe-parser"  % Version.circe
  )

  val catsEffect: List[ModuleID] = List(
    "org.typelevel" %% "cats-effect" % Version.catsEffect
  )

  val fs2: Seq[ModuleID] = List(
    "co.fs2" %% "fs2-core" % Version.fs2,
  )

}
