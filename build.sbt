lazy val commonSettings = List(
  scalaVersion := Lib.Version.scala,
  version      := Lib.Version.service,
  scalacOptions ++= List(
    "-deprecation",
    "-encoding",
    "utf-8",
    "-explaintypes",
    "-feature",
    "-no-indent",
    "-Xfatal-warnings",
    "-Wunused:all",
    "-Wvalue-discard",
  ),
  // Test / parallelExecution := true,
  Compile / packageDoc / mappings := List.empty,
)

lazy val storm = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "storm",
  )
  .aggregate(`lib-common-model`)
  .aggregate(`lib-common-node`)
  .aggregate(`service-echo`)
  .aggregate(`service-unique-id`)
  .aggregate(`service-broadcast`)
  .aggregate(`service-counter`)

lazy val `lib-common-model` = project
  .in(file("lib-common-model"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Lib.circe)

lazy val `lib-common-node` = project
  .in(file("lib-common-node"))
  .settings(commonSettings)
  .settings(
    name := "lib-common-node",
    libraryDependencies ++=
        Lib.catsEffect ++
        Lib.fs2
  )
  .dependsOn(`lib-common-model`)

lazy val `service-echo` = project
  .in(file("service-echo"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    name := "service-echo",
    libraryDependencies ++=
      Lib.logback ++
        Lib.scalaLogging ++
        Lib.config ++
        Lib.catsEffect ++
        Lib.fs2
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.echo.Service")))

lazy val `service-broadcast` = project
  .in(file("service-broadcast"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    name := "service-broadcast",
    libraryDependencies ++=
      Lib.logback ++
        Lib.scalaLogging ++
        Lib.config ++
        Lib.catsEffect ++
        Lib.fs2
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.broadcast.Service")))

lazy val `service-unique-id` = project
  .in(file("service-unique-id"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    name := "service-unique-id",
    libraryDependencies ++=
      Lib.logback ++
        Lib.scalaLogging ++
        Lib.config ++
        Lib.catsEffect ++
        Lib.fs2
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.unique.Service")))

lazy val `service-counter` = project
  .in(file("service-counter"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    name := "service-counter",
    libraryDependencies ++=
      Lib.logback ++
        Lib.scalaLogging ++
        Lib.config ++
        Lib.catsEffect ++
        Lib.fs2
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.counter.Service")))
