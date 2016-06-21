package ch.becompany.akka.io.csv

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.akka.io.file.ResourceReader
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

class CsvReaderSpec extends FlatSpec with Matchers with ScalaFutures {

  import Parsers._

  implicit val system = ActorSystem("UserImporter")
  implicit val materializer = ActorMaterializer()

  case class Animal(name: String, species: String)

  val sources = Seq("animals.csv")
  val spec = CsvSpec(encoding = Some("UTF-8"))

  "CSV reader" should "read CSV files" in {
    /*
    val reader = new Csv[Animal](spec)
    sources foreach { source =>
      val result = ResourceReader.read(source) { src =>
        src.runWith(TestSink.probe[Animal])
          .request(3)
          .expectNext(
            Animal("Bolt", "dog"),
            Animal("Mittens", "cat"),
            Animal("Rhino", "hamster"))
          .expectComplete()
      }
      result shouldBe 'right
    }
    */
  }

}
