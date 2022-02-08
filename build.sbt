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

lazy val server = project
  .settings(
    scalaJSProjects := Seq(client),
    libraryDependencies += guice,
    libraryDependencies += "com.vmunier" %% "scalajs-scripts" % "1.2.0",
    libraryDependencies += "com.lihaoyi" %% "autowire" % "0.3.3",
    libraryDependencies += "com.lihaoyi" %% "upickle" % "1.4.3",
    Assets / pipelineStages  := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .dependsOn(shared.jvm)

lazy val client = project
  .settings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.0.0",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "2.0.0",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "2.0.0",
    libraryDependencies += "com.github.japgolly.scalacss" %%% "ext-react" % "1.0.0",
    libraryDependencies += "com.lihaoyi" %%% "autowire" % "0.3.3",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "1.4.3",
//    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.3.5",
//    libraryDependencies += "co.fs2" %%% "fs2-core" % "3.2.0",
    libraryDependencies += "io.monix" %%% "monix" % "3.4.0",

    Compile / npmDependencies ++= Seq(
      "react" -> "17.0.0",
      "react-dom" -> "17.0.0",
      "ag-grid-react"     -> "26.2.0",
      "ag-grid-community" -> "26.2.0"
    ),
    scalaJSUseMainModuleInitializer := true,
    webpackBundlingMode := BundlingMode.LibraryOnly()
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(shared.js)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .jsConfigure(_.enablePlugins(ScalaJSWeb))
  .settings(
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "1.4.3",
  ).jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided"
  )


onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s} //andThen {s: State => "run" :: s}