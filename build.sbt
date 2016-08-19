import Dependencies._

resolvers ++= Seq(
  sbtResolver.value
)

lazy val akkaFileIo = (
  Project("akka-file-io", file("."))
  settings(
    organization := "ch.becompany",
    name := "akka-file-io",
    version := "1.0.0",
    scalaVersion := "2.11.8",
    libraryDependencies ++= dependencies,

    // github pages
    ghpages.settings,
    git.remoteRepo := "git@github.com:becompany/akka-file-io.git",

    // Publishing
    /*
    useGpg := true,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      isSnapshot.value match {
        case true => Some("snapshots" at nexus + "content/repositories/snapshots")
        case false => Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      }
    },
    pomIncludeRepository := { _ => false },
    */
    pomExtra := (
      <url>https://github.com/becompany/akka-file-io</url>
      <licenses>
        <license>
          <name>Apache License Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:becompany/akka-file-io.git</url>
        <connection>scm:git:git@github.com:becompany/akka-file-io.git</connection>
      </scm>
      <developers>
        <developer>
          <id>devkat</id>
          <name>Andreas Jim-Hartmamm</name>
          <url>https://github.com/devkat</url>
        </developer>
      </developers>),
    sonatypeProfileName := "devkat"
  )
  enablePlugins(SiteScaladocPlugin)
)
