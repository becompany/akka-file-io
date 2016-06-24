package ch.becompany.akka.io.file

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Framing._
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{ActorMaterializer, IOResult, OverflowStrategy}
import akka.util.ByteString
import ch.becompany.akka.io.DetectEncoding
import org.apache.commons.io.input.{Tailer, TailerListenerAdapter}

import scala.concurrent.Future

/**
  * Provides methods to read a file as `akka.stream.scaladsl.Source`.
  */
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
    * Reads a file continuously. Currently it is not possible to stop the reading process.
    *
    * @param path The path.
    * @return Either an error or the source.
    */
  def readContinuously(path: String, end: Boolean, encoding: Option[String] = None): Source[String, _] = {
    val charset = encoding.map(Charset.forName).getOrElse(Charset.defaultCharset)
    Source.queue[String](bufferSize = 1000, OverflowStrategy.fail).
      mapMaterializedValue { queue =>
        Tailer.create(Paths.get(path).toFile, charset, new TailerListenerAdapter {
          override def handle(line: String): Unit = {
            queue.offer(line)
          }
        }, 1000, false, false, 4096)
      }
  }

}
