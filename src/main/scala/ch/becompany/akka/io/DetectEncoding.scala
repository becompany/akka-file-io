package ch.becompany.akka.io

import java.io.InputStream

import org.mozilla.universalchardet.UniversalDetector

object UnsupportedEncoding extends IoError

object DetectEncoding {

  def apply(in: InputStream): Either[IoError, String] =
    try {
      val detector = new UniversalDetector(null)
      var nread: Int = 0
      var buf = new Array[Byte](4096)
      while ({ nread = in.read(buf); nread } > 0 && !detector.isDone()) {
        detector.handleData(buf, 0, nread)
      }
      detector.dataEnd()

      detector.getDetectedCharset() match {
        case null => Left(UnsupportedEncoding)
        case s => Right(s)
      }
    } finally {
      in.close()
    }

}
