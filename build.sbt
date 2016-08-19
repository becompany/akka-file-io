import Dependencies._

resolvers ++= Seq(
  sbtResolver.value
)

lazy val akkaFileIo = (
  Project("akka-file-io", file("."))
  settings(
    organization := "ch.becompany",
    name := "akka-file-io",
    version := "0.1.1-SNAPSHOT",
    scalaVersion := "2.11.8",
    libraryDependencies ++= dependencies,

    // github pages
    ghpages.settings,
    git.remoteRepo := "git@github.com:becompany/akka-file-io.git"
  )
  enablePlugins(SiteScaladocPlugin)
)
