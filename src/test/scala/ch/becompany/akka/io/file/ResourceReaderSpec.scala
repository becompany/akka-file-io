package ch.becompany.akka.io.file

import java.nio.charset.StandardCharsets
import java.nio.file._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class ResourceReaderSpec extends FlatSpec with Matchers with ScalaFutures {

  implicit val system = ActorSystem("akka-file-io")
  implicit val materializer = ActorMaterializer()

  val resource = "/ch/becompany/akka/io/csv/animals.csv"

  "Resource reader" should "read a resource" in {
    ResourceReader.read(resource, Some("UTF-8")).
      runWith(TestSink.probe[String]).
      request(3).
      expectNext(
        "Bolt, dog",
        "Mittens, cat",
        "Rhino, hamster").
      expectComplete()
  }

}
