import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.webpackBundlingMode

ThisBuild / organization := "sysmo"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .aggregate(server, client, macros, shared.jvm, shared.js)

lazy val macros = project
  .settings(
    libraryDependencies += "org.scalameta" %% "scalameta" % "4.4.33"
  )


lazy val reform_back = project
  .settings(
    // Logging
//    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.36",
//    libraryDependencies += "org.slf4j" % "slf4j-reload4j" % "1.7.36",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10",
//    libraryDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.1",
    libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.9.0",
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.6",

    // Arrow
    libraryDependencies += "org.apache.arrow" % "arrow-vector" % "6.0.1",
    libraryDependencies += "org.apache.arrow" % "arrow-memory-netty" % "6.0.1" % "runtime",
    libraryDependencies += "org.apache.arrow" % "arrow-dataset" % "6.0.1",
    // kantan CSV IO
    libraryDependencies += "com.nrinaudo" %% "kantan.csv" % "0.6.1",
    libraryDependencies += "com.nrinaudo" %% "kantan.csv-generic" % "0.6.1",
    // Slick
    libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.3",
    // Scalike
    libraryDependencies += "org.scalikejdbc" %% "scalikejdbc"       % "3.5.0",
//    libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.6.4",
    libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
    // SQLite
    libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.36.0.3",
    // Monix
    libraryDependencies += "io.monix" %% "monix" % "3.4.0",
    // OrientDB
    libraryDependencies += "com.orientechnologies" % "orientdb-client" % "3.2.5",
    libraryDependencies += "com.orientechnologies" % "orientdb-gremlin" % "3.2.5",
    // POI
    libraryDependencies += "org.apache.poi" % "poi" % "5.2.0",
    libraryDependencies += "org.apache.poi" % "poi-ooxml" % "5.2.0",

    libraryDependencies += "com.lihaoyi" %% "autowire" % "0.3.3",


    libraryDependencies += "com.lihaoyi" % "ammonite" % "2.5.2" % "test" cross CrossVersion.full,
    //    libraryDependencies += {
//      val version = scalaBinaryVersion.value match {
//        case "2.10" => "1.0.3"
//        case "2.11" => "1.6.7"
//        case _ ⇒ "2.5.2"
//      }
//      "com.lihaoyi" % "ammonite" % version % "test" cross CrossVersion.full
//    },
//
    Test / sourceGenerators += Def.task {
      val file = (Test / sourceManaged).value / "amm.scala"
      IO.write(file, """object amm extends App { ammonite.AmmoniteMain.main(args) }""")
      Seq(file)
    }.taskValue,

  ).dependsOn(shared.jvm)

lazy val server = project
  .settings(
    scalaJSProjects := Seq(client),
    libraryDependencies += guice,
    libraryDependencies += "com.vmunier" %% "scalajs-scripts" % "1.2.0",
//    libraryDependencies += "com.lihaoyi" %% "upickle" % "1.4.3",
    Assets / pipelineStages  := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .dependsOn(reform_back)

lazy val client = project
  .settings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.0.0",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "2.0.0",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "2.0.0",
    libraryDependencies += "com.github.japgolly.scalacss" %%% "ext-react" % "1.0.0",
    libraryDependencies += "com.lihaoyi" %%% "autowire" % "0.3.3",
//    libraryDependencies += "com.lihaoyi" %%% "upickle" % "1.4.3",
//    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.3.5",
//    libraryDependencies += "co.fs2" %%% "fs2-core" % "3.2.0",
    libraryDependencies += "io.monix" %%% "monix" % "3.4.0",

    Compile / npmDependencies ++= Seq(
      "react" -> "17.0.0",
      "react-dom" -> "17.0.0",
      "ag-grid-react"     -> "26.2.0",
      "ag-grid-community" -> "26.2.0",
      "react-select" ->  "5.2.2",
    ),
    scalaJSUseMainModuleInitializer := true,
    webpackBundlingMode := BundlingMode.LibraryOnly(),
    webpackEmitSourceMaps := false
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(shared.js)

val circeVersion = "0.14.1"

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .jsConfigure(_.enablePlugins(ScalaJSWeb))
  .settings(
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "1.4.3",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-generic-extras"
    ).map(_ % circeVersion)
  ).jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided"
  ).jsSettings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser",
      "io.circe" %%% "circe-generic-extras"
    ).map(_ % circeVersion)

  )


onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s} //andThen {s: State => "run" :: s}