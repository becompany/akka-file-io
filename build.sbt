import Dependencies._

resolvers ++= Seq(
  sbtResolver.value
)

lazy val akkaFileIo = (
  Project("akka-file-io", file("."))
  settings(
    organization := "ch.becompany",
    name := "akka-file-io",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.8",
    libraryDependencies ++= dependencies
  )
)
