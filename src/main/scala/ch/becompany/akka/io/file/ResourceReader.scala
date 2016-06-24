package ch.becompany.akka.io.file

import akka.stream.IOResult
import akka.stream.scaladsl.Framing._
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.StreamConverters._
import akka.util.ByteString
import ch.becompany.akka.io.DetectEncoding

import scala.concurrent.Future

/**
  * Provides methods to read a classpath resource as an `akka.stream.scaladsl.Source`.
  */
object ResourceReader {

  /**
    * Reads from a resource.
    *
    * @param name The name.
    * @param encoding The optional encoding, detected by default.
    * @return Either an error or the source.
    */
  def read(name: String, encoding: Option[String] = None): Source[String, Future[IOResult]] = {
    def inputStream() =
      getClass.getResourceAsStream(name)
    val enc = encoding.getOrElse(DetectEncoding(inputStream()))
    fromInputStream(inputStream).
      via(delimiter(ByteString("\n"), Int.MaxValue)).
      map(_.decodeString(enc))
  }

}
