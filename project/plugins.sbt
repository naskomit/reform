// Comment to get more information during initialization
logLevel := Level.Warn

addSbtPlugin("com.vmunier"               % "sbt-web-scalajs"           % "1.2.0")
addSbtPlugin("org.scala-js"              % "sbt-scalajs"               % "1.8.0")
addSbtPlugin("com.typesafe.play"         % "sbt-plugin"                % "2.8.13")
addSbtPlugin("org.portable-scala"        % "sbt-scalajs-crossproject"  % "1.1.0")
addSbtPlugin("com.typesafe.sbt"          % "sbt-gzip"                  % "1.0.2")
addSbtPlugin("com.typesafe.sbt"          % "sbt-digest"                % "1.1.4")
addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.20.0")
addDependencyTreePlugin

libraryDependencies ++= Seq(
  "org.scalameta" %% "scalameta" % "4.5.5"
)
ThisBuild / libraryDependencySchemes ++= Seq(
    "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
)
