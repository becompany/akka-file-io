package ch.becompany.akka.io.file

import java.io.InputStream

import akka.stream.scaladsl.Framing._
import akka.stream.scaladsl.{FlowOps, Source}
import akka.util.ByteString
import ch.becompany.akka.io.{DetectEncoding, IoError}

trait Reader {

  protected def getEncoding(in: => InputStream, default: Option[String] = None): Either[IoError, String] =
    default.fold(DetectEncoding(in))(Right(_))

  protected def readLines[T <: FlowOps[ByteString, _]](flow: T, encoding: String): T#Repr[String] =
    flow.
      via(delimiter(ByteString("\n"), Int.MaxValue)).
      map(_.decodeString(encoding)).
      asInstanceOf[T#Repr[String]]

}
