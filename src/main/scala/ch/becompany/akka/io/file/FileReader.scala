package ch.becompany.akka.io.file

import java.nio.file.{Files, Path, Paths}

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Framing._
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import ch.becompany.akka.io.DetectEncoding

import scala.concurrent.Future

object FileReader {

  implicit val system = ActorSystem("akka-file-io")
  implicit val materializer = ActorMaterializer()

  private def withFile[T](path: String, encoding: Option[String])(f: (Path, String) => T): T = {
    val pathObj = Paths.get(path)
    val enc = encoding.getOrElse(DetectEncoding(Files.newInputStream(pathObj)))
    f(pathObj, enc)
  }

  /**
    * Reads a file.
    *
    * @param path The path.
    * @param encoding The optional encoding, detected by default.
    * @return Either an error or the source.
    */
  def read(path: String, encoding: Option[String] = None): Source[String, Future[IOResult]] =
    withFile(path, encoding) { (path, enc) =>
      FileIO.fromPath(path).
        via(delimiter(ByteString("\n"), Int.MaxValue)).
        map(_.decodeString(enc))
    }

  /**
    * Read a file continuously.
    *
    * @param path The path.
    * @return Either an error or the source.
    */
  def readContinuously(path: String, end: Boolean, encoding: Option[String] = None): Source[String, NotUsed] =
    withFile(path, encoding) { (path, enc) =>
      val tailPublisher = system.actorOf(Props[TailPublisher])
      val src = Source.
        fromPublisher(ActorPublisher[ByteString](tailPublisher)).
        map(_.decodeString(enc))
      new Tailer(path, enc, end, tailPublisher).run()
      src
    }

}
