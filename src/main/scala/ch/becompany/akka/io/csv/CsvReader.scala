package ch.becompany.akka.io.csv

import java.io.{File, FileInputStream, InputStream}
import java.nio.file.{Files, Paths}

import akka.stream.IOResult
import akka.stream.scaladsl.FileIO._
import akka.stream.scaladsl.Framing._
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.StreamConverters._
import akka.util.ByteString
import ch.becompany.akka.io.{DetectEncoding, IoError, SourceNotFound}
import org.mozilla.universalchardet.UniversalDetector
import shapeless._
import shapeless.ops.traversable.FromTraversable
import shapeless.ops.traversable.FromTraversable._
import syntax.std.traversable._

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class CsvReader[T <: HList](spec: CsvSpec = CsvSpec()) {

  /**
    * Reads a CSV from a file.
    * @param path The path.
    * @return Either an error or the CSV source.
    */
  def fromFile(path: String): Either[IoError, Source[T, Future[IOResult]]] = {
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
  def fromResource(name: String): Either[IoError, Source[T, Future[IOResult]]] = {
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
      Source[T, Future[IOResult]] =
    source.
      via(delimiter(ByteString(spec.lineDelimiter), Int.MaxValue)).
      map(_.decodeString(encoding)).
      map(readLine).
      map(parseLine)

  private def readLine(line: String): List[Option[String]] =
    spec.fieldDelimiter.split(line).
      toList.
      map(_.trim).
      map(s => if (s.isEmpty) None else Some(s))

  private def parseLine(line: List[Option[String]]): T = {
    implicit val fl = FromTraversable[T]
    line.toHList[T].get
  }
}
