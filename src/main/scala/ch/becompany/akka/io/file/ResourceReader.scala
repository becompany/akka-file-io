package ch.becompany.akka.io.file

import akka.stream.IOResult
import akka.stream.scaladsl.Framing._
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.StreamConverters._
import akka.util.ByteString
import ch.becompany.akka.io.DetectEncoding

import scala.concurrent.Future

object ResourceReader {

  /**
    * Reads from a resource.
    *
    * @param name The name.
    * @return Either an error or the source.
    */
  def read(name: String): Source[String, Future[IOResult]] = {
    val inputStream = getClass.getResourceAsStream(name)
    val enc = DetectEncoding(inputStream)
    fromInputStream(() => inputStream).
      via(delimiter(ByteString("\n"), Int.MaxValue)).
      map(_.decodeString(enc))
  }

}
