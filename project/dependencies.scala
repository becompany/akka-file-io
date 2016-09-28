import sbt._

object Dependencies {

  object versions {
    val scalaTest = "2.2.6"
    val jUniversalChardet = "1.0.3"
    val akka = "2.4.10"
    val shapeless = "2.3.1"
    val scalaCsv = "1.3.1"
    val commonsIo = "2.5"
    val cats = "0.6.1"
  }

  val scalactic = "org.scalactic" %% "scalactic" % versions.scalaTest
  val scalaTest = "org.scalatest" %% "scalatest" % versions.scalaTest % "test"
  val jUniversalChardet = "com.googlecode.juniversalchardet" % "juniversalchardet" % versions.jUniversalChardet
  val akkaStreams = "com.typesafe.akka" %% "akka-stream" % versions.akka
  val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % versions.akka
  val shapeless = "com.chuusai" %% "shapeless" % versions.shapeless
  val scalaCsv = "com.github.tototoshi" %% "scala-csv" % versions.scalaCsv
  val commonsIo = "commons-io" % "commons-io" % versions.commonsIo
  val cats = "org.typelevel" %% "cats" % versions.cats
  
  val dependencies = Seq(
    scalactic,
    scalaTest,
    jUniversalChardet,
    akkaStreams,
    akkaStreamTestkit,
    shapeless,
    scalaCsv,
    commonsIo,
    cats
  )

}
