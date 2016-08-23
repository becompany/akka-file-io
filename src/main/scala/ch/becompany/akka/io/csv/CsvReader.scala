package ch.becompany.akka.io.csv

import akka.stream.scaladsl.{FlowOps, Source}
import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{invalid, valid}
import com.github.tototoshi.csv.{CSVParser, DefaultCSVFormat, QUOTE_MINIMAL, Quoting}

class CsvReader[T](spec: CsvSpec = CsvSpec())(implicit parser: LineParser[T]) {

  private val commentPattern = "^\\s+#".r

  private lazy val lineParser = new CSVParser(new DefaultCSVFormat() {
    override val delimiter: Char = spec.fieldDelimiter
    override val quoteChar: Char = spec.quote
    override val quoting: Quoting = QUOTE_MINIMAL
  })

  private def parseLine(line: String): LineResult[T] = {
    lineParser.parseLine(line) match {
      case Some(fields) => LineParser[T](fields.map(_.trim))
      case None => invalid(NonEmptyList(s"Invalid line: $line"))
    }
  }

  /**
    * Transforms a flow of strings into a flow of `Try`s of CSV records.
    *
    * @param source The source.
    * @tparam Mat The materialized value type.
    * @return The transformed source.
    */
  def read[Mat](source: Source[String, Mat]): Source[LineResult[T], Mat] =
    source.
      filterNot(commentPattern.findFirstIn(_).isDefined).
      map(parseLine)

}
