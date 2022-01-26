ThisBuild / organization := "sysmo"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .aggregate(server, client, shared.jvm, shared.js)

lazy val server = project
  .settings(
    scalaJSProjects := Seq(client),
    libraryDependencies += guice,
    libraryDependencies += "com.vmunier" %% "scalajs-scripts" % "1.2.0",
    Assets / pipelineStages  := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .dependsOn(shared.jvm)

lazy val client = project
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.0.0",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "2.0.0",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "2.0.0",
    libraryDependencies += "com.github.japgolly.scalacss" %%% "ext-react" % "1.0.0",
    Compile / npmDependencies ++= Seq(
      "react" -> "17.0.2",
      "react-dom" -> "17.0.2")
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(shared.js)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .jsConfigure(_.enablePlugins(ScalaJSWeb))

onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}