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
  .aggregate(`service-controller`)
  .aggregate(`service-echo`)
  .aggregate(`service-unique-id`)
  .aggregate(`service-broadcast`)
  .aggregate(`service-counter`)
  .aggregate(`service-kafka`)
  .aggregate(`service-txn`)

lazy val `lib-common-model` = project
  .in(file("lib/lib-common-model"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Lib.circe)

lazy val `lib-common-node` = project
  .in(file("lib/lib-common-node"))
  .settings(commonSettings)
  .settings(
    name := "lib-common-node",
    libraryDependencies ++=
      Lib.config ++
        Lib.catsEffect ++
        Lib.fs2
  )
  .dependsOn(`lib-common-model`)

lazy val `service-controller` = project
  .in(file("mod/service-controller"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(commonSettings)
  .settings(
    name := "service-controller",
  )
  .settings(
    graalVMNativeImageOptions ++= List(
      "-H:IncludeResources=(reference|application).conf$",
      "--no-fallback",
    )
  )
  .settings(
    libraryDependencies ++= Lib.decline,
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.controller.Service")))

lazy val `service-echo` = project
  .in(file("mod/service-echo"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    name := "service-echo",
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.echo.Service")))

lazy val `service-broadcast` = project
  .in(file("mod/service-broadcast"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(commonSettings)
  .settings(
    name := "service-broadcast",
  )
  .settings(
    graalVMNativeImageOptions ++= List(
      "-H:IncludeResources=(reference|application).conf$",
      "--no-fallback",
    )
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.broadcast.Service")))

lazy val `service-unique-id` = project
  .in(file("mod/service-unique-id"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    name := "service-unique-id",
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.unique.Service")))

lazy val `service-counter` = project
  .in(file("mod/service-counter"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    name := "service-counter",
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.counter.Service")))

lazy val `service-kafka` = project
  .in(file("mod/service-kafka"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    name := "service-kafka",
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.kafka.Service")))

lazy val `service-txn` = project
  .in(file("mod/service-txn"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    name := "service-txn",
  )
  .dependsOn(`lib-common-node`)
  .settings(List(Compile / mainClass := Some("storm.txn.Service")))


