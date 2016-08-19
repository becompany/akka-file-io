package ch.becompany.akka.io.csv

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import org.scalatest._

class LineParserSpec extends FlatSpec with Matchers with EitherValues {

  import Parsers._

  case class Record(a: String, b: String)

  "A LineParser" should "parse lines" in {
    val line = List("foo", "bar")
    val result = LineParser[Record](line)
    result shouldBe Valid(Record("foo", "bar"))
  }

  "A LineParser" should "fail on missing elements" in {
    val line = List("foo")
    val result = LineParser[Record](line)
    result shouldBe Invalid(NonEmptyList("Excepted list element."))
  }

  "A LineParser" should "fail on surplus elements" in {
    val line = List("foo", "bar", "baz")
    val result = LineParser[Record](line)
    result shouldBe Invalid(NonEmptyList("""Expected end, got "baz"."""))
  }

}
