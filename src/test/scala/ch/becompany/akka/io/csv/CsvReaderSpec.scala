package ch.becompany.akka.io.csv

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import com.github.tototoshi.csv.{CSVParser, DefaultCSVFormat, QUOTE_MINIMAL, Quoting}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

class CsvReaderSpec extends FlatSpec with Matchers with ScalaFutures {

  import Parsers._

  implicit val system = ActorSystem("UserImporter")
  implicit val materializer = ActorMaterializer()

  case class Animal(name: String, species: String)

  val sources = Seq("animals.csv")
  val spec = CsvSpec(encoding = Some("UTF-8"))

  private lazy val lineParser = new CSVParser(new DefaultCSVFormat() {
    override val delimiter: Char = spec.fieldDelimiter
    override val quoteChar: Char = spec.quote
    override val quoting: Quoting = QUOTE_MINIMAL
  })

  def parseLine(line: String): Either[String, List[String]] = {
    lineParser.parseLine(line) match {
      case Some(fields) => Right(fields)
      case None => Left(s"Invalid line: $line")
    }
  }

  "CSV reader" should "read CSV files" in {
    val reader = new CsvReader[Animal](spec, parseLine)
    sources foreach { source =>
      val result = reader.fromResource(source)
      result shouldBe 'right
      result.right.foreach { source =>
        source.runWith(TestSink.probe[Animal])
          .request(3)
          .expectNext(
            Animal("Bolt", "dog"),
            Animal("Mittens", "cat"),
            Animal("Rhino", "hamster"))
          .expectComplete()
      }
    }
  }

}
