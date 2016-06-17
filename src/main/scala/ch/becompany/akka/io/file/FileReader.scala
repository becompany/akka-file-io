package ch.becompany.akka.io.file

import java.io.InputStream
import java.nio.file.{Files, Path, Paths}

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.Framing._
import akka.stream.scaladsl.{FileIO, Flow, FlowOps, Sink, Source}
import akka.util.ByteString
import ch.becompany.akka.io.{DetectEncoding, IoError, Processor, SourceNotFound}

import scala.concurrent.Future
import scala.concurrent.duration._

class FileReader extends Reader {

  implicit val system = ActorSystem("akka-file-io")
  implicit val materializer = ActorMaterializer()

  private def withFile[T](path: String)(f: (Path, String) => T): Either[IoError, T] = {
    val pathObj = Paths.get(path)
    if (Files.isRegularFile(pathObj)) {
      getEncoding(Files.newInputStream(pathObj)).right.map { enc =>
        f(pathObj, enc)
      }
    } else {
      Left(SourceNotFound)
    }
  }

  /**
    * Processes a file.
    *
    * @param path The path.
    * @return Either an error or the source.
    */
  def read(path: String, proc: Processor[String, Future[IOResult]]): Either[IoError, Future[IOResult]] =
    withFile(path) { (path, enc) =>
      val src = readLines(FileIO.fromPath(path), enc)
      val f = proc.apply(src)
      f.to(Sink.ignore).run()
    }

  /**
    * Processes a file continuously.
    *
    * @param path The path.
    * @return Either an error or the source.
    */
  def readContinuously[T](path: String, proc: Processor[String, Future[IOResult]]): Either[IoError, Unit] =
    withFile(path) { (path, enc) =>
      val src = Source.actorPublisher[ByteString](Props[TailPublisher])
      val flow = readLines(Flow[ByteString], enc)
      val ref = proc.apply(flow).to(Sink.ignore).runWith(src)
      new Tailer(path, enc, ref).run()
    }

}
