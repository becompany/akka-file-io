package ch.becompany.akka.io.csv

import java.io.InputStream
import java.nio.file.{Files, Paths}

import akka.stream.IOResult
import akka.stream.scaladsl.FileIO._
import akka.stream.scaladsl.Framing._
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.StreamConverters._
import akka.util.ByteString
import ch.becompany.akka.io.{DetectEncoding, IoError, SourceNotFound}
import com.github.tototoshi.csv.{CSVParser, DefaultCSVFormat, QUOTE_MINIMAL, Quoting}
import shapeless._

import scala.concurrent.Future

class CsvReader[L](spec: CsvSpec = CsvSpec(), splitLine: String => Either[String, List[String]])(implicit parser: LineParser[L]) {

  /**
    * Reads a CSV from a file.
    * @param path The path.
    * @return Either an error or the CSV source.
    */
  def fromFile(path: String): Either[IoError, Source[L, Future[IOResult]]] = {
    val pathObj = Paths.get(path)
    if (Files.isRegularFile(pathObj)) {
      getEncoding(Files.newInputStream(pathObj)).right.map { enc =>
        read(fromPath(pathObj), enc)
      }
    } else {
      Left(SourceNotFound)
    }
  }

  /**
    * Reads a CSV from a file.
    * @param path The path.
    * @return Either an error or the CSV source.
    */
  def fromResource(name: String): Either[IoError, Source[L, Future[IOResult]]] = {
    def inputStream = getClass.getResourceAsStream(name)
    if (inputStream == null) {
      Left(SourceNotFound)
    } else {
      getEncoding(inputStream).right.map { enc =>
        read(fromInputStream(inputStream _), enc)
      }
    }
  }

  def getEncoding(in: => InputStream): Either[IoError, String] =
    spec.encoding.fold(DetectEncoding(in))(Right(_))

  private def read(source: Source[ByteString, Future[IOResult]], encoding: String):
      Source[L, Future[IOResult]] =
    source.
      via(delimiter(ByteString(spec.lineDelimiter), Int.MaxValue)).
      map(_.decodeString(encoding)).
      map(parseLine).
      map(_.fold[Option[L]](errors => { println(errors); None }, Some(_))).
      filter(_.isDefined).
      map(_.get)

  private def parseLine(line: String): Either[List[String], L] =
    splitLine(line) match {
      case Right(fields) => LineParser[L](fields.map(_.trim))
      case Left(error) => Left(List(error))
    }

}
