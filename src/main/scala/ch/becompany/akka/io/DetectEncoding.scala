package ch.becompany.akka.io

import java.io.{InputStream, UnsupportedEncodingException}

import org.mozilla.universalchardet.UniversalDetector

import scala.util.{Failure, Success, Try}

object UnsupportedEncoding extends IoError

object DetectEncoding {

  def apply(in: InputStream): String =
    try {
      val detector = new UniversalDetector(null)
      var nread: Int = 0
      var buf = new Array[Byte](4096)
      while ({ nread = in.read(buf); nread } > 0 && !detector.isDone()) {
        detector.handleData(buf, 0, nread)
      }
      detector.dataEnd()

      detector.getDetectedCharset() match {
        case null => throw new UnsupportedEncodingException()
        case s => s
      }
    } finally {
      in.close()
    }

}
