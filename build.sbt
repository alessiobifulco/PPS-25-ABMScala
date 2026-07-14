ThisBuild / version      := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.8"

lazy val root = project
  .in(file("."))
  .settings(
    name := "PPS-25-ABMScala",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % Test,
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    fork := true,
    assembly / assemblyJarName := "ABMScala.jar"
  )
