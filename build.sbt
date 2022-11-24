import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.webpackBundlingMode
import com.typesafe.sbt.packager.docker._

ThisBuild / organization := "sysmo"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version      := "0.1.2"
ThisBuild / maintainer   := "Atanas Pavlov"

ThisBuild / javaOptions ++= Seq(
  "-Dpidfile.path=/dev/null",
  "-Dplay.http.secret.key='djdsgfldfjnglkwemf;lsdfnsv lk123453lksdvnsdfvkndxcv;ldf'"
)

val test_libs = Seq(
  "org.scalactic" %% "scalactic" % "3.2.12",
  "org.scalatest" %% "scalatest" % "3.2.12" % "test",
)


//addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

/** =================== Reform =================== */

lazy val root = (project in file("."))
  .aggregate(shared.jvm, shared.js, backend)

val circeVersion = "0.14.1"

// lazy val macros = project
//   .settings(
//     libraryDependencies += "org.scalameta" %% "scalameta" % "4.4.33"
//   )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser",
      "io.circe" %%% "circe-generic-extras",
    ).map(_ % circeVersion),
//    libraryDependencies ++= Seq(
//      "com.lihaoyi" %%% "pprint" % "0.7.0"
//    ),
    libraryDependencies ++= test_libs,
  )

lazy val backend = project
   .settings(
     libraryDependencies ++= test_libs,
     //Config
     libraryDependencies += "com.typesafe" % "config" % "1.4.2",
     // OrientDB
     libraryDependencies += "com.orientechnologies" % "orientdb-client" % "3.2.5",
      // CSV
     libraryDependencies += "com.nrinaudo" %% "kantan.csv" % "0.6.1",
     libraryDependencies += "com.nrinaudo" %% "kantan.csv-generic" % "0.6.1",
     Test / unmanagedSourceDirectories += baseDirectory.value / "../shared/src/test/scala"
   ).dependsOn(shared.jvm)

 lazy val frontend = project
   .settings(
     libraryDependencies ++= Seq(
       "org.scala-js" %%% "scalajs-dom" % "2.0.0",
       "org.scala-js" %%% "scala-js-macrotask-executor" % "1.0.0",

       "com.github.japgolly.scalajs-react" %%% "core" % "2.0.0",
       "com.github.japgolly.scalajs-react" %%% "extra" % "2.0.0",
       "com.github.japgolly.scalacss" %%% "ext-react" % "1.0.0",
     ),
     Compile / npmDependencies ++= Seq(
       "react" -> "17.0.0",
       "react-dom" -> "17.0.0",
       "ag-grid-react"     -> "26.2.0",
       "ag-grid-community" -> "26.2.0",
       "react-select" ->  "5.2.2",
       "plotly.js" -> "1.47.4",
       "mermaid" -> "8.14.0",
       "react-transition-group" -> "4.4.2",
       "react-notifications" -> "1.7.4",
     ),
   )
   .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
   .dependsOn(shared.js)

/** =================== Demo 1 =================== */
lazy val demo1_frontend = project
  .in(file("apps/demo1/frontend"))
  .settings(
    scalaJSLinkerConfig ~= { _.withOptimizer(false) },
    scalaJSUseMainModuleInitializer := true,
    webpackBundlingMode := BundlingMode.LibraryOnly(),
    webpackEmitSourceMaps := true
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(frontend)

lazy val demo1_backend = project
  .in(file("apps/demo1/backend"))
  .settings(
    scalaJSProjects := Seq(demo1_frontend),
    libraryDependencies ++= Seq(
      guice,
      "com.vmunier" %% "scalajs-scripts" % "1.2.0",
      //      "org.slf4j" % "slf4j-api" % "2.0.3",
      //      "ch.qos.logback" % "logback-classic" % "1.4.4" % Test
    ),
    npmAssets ++= NpmAssets.ofProject(frontend) { nodeModules =>
      (nodeModules / "react-notifications" / "lib").allPaths
    }.value,
    Assets / pipelineStages  := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,

    Docker / packageName:= "smo-reform",
    Docker / version:= (ThisBuild / version).value,
    Docker / maintainer:= (ThisBuild / maintainer).value,
    Docker / daemonUserUid := Some("2001"),
    Docker / daemonUser := "dduser",

    dockerRepository := Some("naskomit"),
    dockerChmodType := DockerChmodType.UserGroupWriteExecute,
    dockerBaseImage := "eclipse-temurin:11-jre-alpine",
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      ExecCmd("RUN", "apk", "add", "--no-cache", "bash"),
      Cmd("USER", "dduser"),
    ),
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin, DockerPlugin)
  .dependsOn(backend)

onLoad in Global := (onLoad in Global).value andThen {s: State => "project demo1_backend" :: s} //andThen {s: State => "run" :: s}




//     // Logging
// //    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.36",
// //    libraryDependencies += "org.slf4j" % "slf4j-reload4j" % "1.7.36",
//     // Testing
//     // libraryDependencies ++= test_libs,

// //     libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10",
// // //    libraryDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.1",
// //     libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.9.0",
// //     libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.6",

// //     // Arrow
// //     libraryDependencies += "org.apache.arrow" % "arrow-vector" % "6.0.1",
// //     libraryDependencies += "org.apache.arrow" % "arrow-memory-netty" % "6.0.1" % "runtime",
// //     libraryDependencies += "org.apache.arrow" % "arrow-dataset" % "6.0.1",
// //     // kantan CSV IO
// //     libraryDependencies += "com.nrinaudo" %% "kantan.csv" % "0.6.1",
// //     libraryDependencies += "com.nrinaudo" %% "kantan.csv-generic" % "0.6.1",
// //     // Slick
// //     libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.3",
// //     // Scalike
// //     libraryDependencies += "org.scalikejdbc" %% "scalikejdbc"       % "3.5.0",
// // //    libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.6.4",
// //     libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
// //     // SQLite
// //     libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.36.0.3",
// //     // Monix
// //     libraryDependencies += "io.monix" %% "monix" % "3.4.0",
// //     // Tinkergraph
// //     libraryDependencies += "org.apache.tinkerpop" % "tinkergraph-gremlin" % "3.4.7",
// //     // OrientDB
// //     libraryDependencies += "com.orientechnologies" % "orientdb-client" % "3.2.5",
// //     libraryDependencies += "com.orientechnologies" % "orientdb-gremlin" % "3.2.5",
// //     // POI
// //     libraryDependencies += "org.apache.poi" % "poi" % "5.2.0",
// //     libraryDependencies += "org.apache.poi" % "poi-ooxml" % "5.2.0",

// //     // Plotly
// //     libraryDependencies += "org.plotly-scala" %% "plotly-almond" % "0.8.1",

//     // // Statistics with Breeze
//     // libraryDependencies += "org.scalanlp" %% "breeze" % "2.0.1-RC1",

//     // // Ammonite REPL
//     // libraryDependencies += "com.lihaoyi" % "ammonite" % "2.5.2" % "test" cross CrossVersion.full,

//     // Test / sourceGenerators += Def.task {
//     //   val file = (Test / sourceManaged).value / "amm.scala"
//     //   IO.write(file, """object amm extends App { ammonite.AmmoniteMain.main(args) }""")
//     //   Seq(file)
//     // }.taskValue,

//   ).dependsOn(reform_shared.jvm)