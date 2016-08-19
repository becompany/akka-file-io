package ch.becompany.akka.io.csv

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.akka.io.file.ResourceReader
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

class CsvReaderSpec extends FlatSpec with Matchers {

  import Parsers._

  implicit val system = ActorSystem("UserImporter")
  implicit val materializer = ActorMaterializer()

  case class Animal(name: String, age: Int, species: String)

  val spec = CsvSpec(encoding = Some("UTF-8"))
  val reader = new CsvReader[Animal](spec)
  val resource = "/ch/becompany/akka/io/csv/animals.csv"

  "CSV reader" should "read CSV files" in {
    val src = ResourceReader.read(resource, Some("UTF-8"))
    reader.read(src).
      runWith(TestSink.probe[Animal]).
      request(3).
      expectNext(
        Animal("Bolt", 3, "dog"),
        Animal("Mittens", 2, "cat"),
        Animal("Rhino", 1, "hamster")).
      expectComplete()
  }

}
