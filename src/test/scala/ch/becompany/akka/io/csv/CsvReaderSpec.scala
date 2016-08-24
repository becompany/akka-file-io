package ch.becompany.akka.io.csv

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import cats.data.Validated.{Invalid, Valid}
import cats.data.NonEmptyList
import ch.becompany.akka.io.file.ResourceReader
import org.scalatest.{FlatSpec, Matchers}

class CsvReaderSpec extends FlatSpec with Matchers {

  import Parsers._

  implicit val system = ActorSystem("UserImporter")
  implicit val materializer = ActorMaterializer()

  case class Animal(name: String, age: Int, species: String)

  val reader = new CsvReader[Animal]

  val lines = Seq(
    "Bolt, 3, dog",
    "Mittens, 2, cat",
    "Rhino, 1, hamster"
  )

  "CSV reader" should "read CSV files" in {
    val src = Source.fromIterator(() => lines.iterator)
    reader.read(src).
      runWith(TestSink.probe[LineResult[Animal]]).
      request(3).
      expectNext(
        Valid(Animal("Bolt", 3, "dog")),
        Valid(Animal("Mittens", 2, "cat")),
        Valid(Animal("Rhino", 1, "hamster"))).
      expectComplete()
  }

  "CSV reader" should "ignore comments" in {
    val src = Source.fromIterator(() => (" # comment" +: lines).iterator)
    reader.read(src).
      runWith(TestSink.probe[LineResult[Animal]]).
      request(3).
      expectNext(
        Valid(Animal("Bolt", 3, "dog")),
        Valid(Animal("Mittens", 2, "cat")),
        Valid(Animal("Rhino", 1, "hamster"))).
      expectComplete()
  }

  "CSV reader" should "emit field errors" in {
    val src = Source.single("Bolt, foo, dog")
    reader.read(src).
      runWith(TestSink.probe[LineResult[Animal]]).
      request(1).
      expectNext(
        Invalid(NonEmptyList("""[age] java.lang.NumberFormatException For input string: "foo""""))).
      expectComplete()
  }

}
