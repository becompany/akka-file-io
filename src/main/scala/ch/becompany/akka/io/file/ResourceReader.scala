package ch.becompany.akka.io.file

import akka.stream.IOResult
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.StreamConverters._
import ch.becompany.akka.io.{IoError, SourceNotFound}

import scala.concurrent.Future

class ResourceReader extends Reader {

  /**
    * Reads from a resource.
    *
    * @param name The name.
    * @return Either an error or the source.
    */
  def fromResource(name: String): Either[IoError, Source[String, Future[IOResult]]] = {
    def inputStream = getClass.getResourceAsStream(name)
    if (inputStream == null) {
      Left(SourceNotFound)
    } else {
      getEncoding(inputStream).right.map { enc =>
        read(fromInputStream(inputStream _), enc)
      }
    }
  }

}
