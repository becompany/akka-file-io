import sbt._

object Dependencies {
  
  val scalaTestVersion = "2.2.6"
  val jUniversalChardetVersion = "1.0.3"
  val akkaVersion = "2.4.6"
  val shapelessVersion = "2.3.1"

  val scalactic = "org.scalactic" %% "scalactic" % scalaTestVersion
  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  val jUniversalChardet = "com.googlecode.juniversalchardet" % "juniversalchardet" % jUniversalChardetVersion
  val akkaStreams = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
  val shapeless = "com.chuusai" %% "shapeless" % shapelessVersion
  
  val dependencies = Seq(
    scalactic,
    scalaTest,
    jUniversalChardet,
    akkaStreams,
    akkaStreamTestkit,
    shapeless
  )

}
