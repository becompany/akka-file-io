package ch.becompany.akka.io.file

import java.nio.file.{Files, Path, Paths}

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Flow, Source}
import akka.util.{ByteString, Timeout}
import ch.becompany.akka.io.{IoError, SourceNotFound}
import org.apache.commons.io.input.{Tailer, TailerListenerAdapter}

import scala.annotation.tailrec
import scala.concurrent.duration._

class Tailer(val path: Path, val encoding: String, val publisher: ActorRef)
  extends TailerListenerAdapter {

  import TailPublisher._
  implicit val timeout = Timeout(1 second)

  override def handle(line: String): Unit = {
    println(s"handle $line")
    publisher ! Line(ByteString(line, encoding))
  }

  override def handle(e: Exception): Unit = {
    e.printStackTrace(System.out)
    publisher ! Error(e)
  }

  def run(): Unit = {
    Tailer.create(path.toFile, this)
  }

}

object TailPublisher {
  def props: Props = Props[TailPublisher]

  case class Line(l: ByteString)
  case class Error(t: Throwable)

  case object LineAccepted
  case object LineDenied

}

class TailPublisher extends ActorPublisher[ByteString] {

  import akka.stream.actor.ActorPublisherMessage._
  import TailPublisher._

  val MaxBufferSize = 1000
  var buf = Vector.empty[ByteString]

  def receive = {
    case Line(_) if buf.size == MaxBufferSize =>
      println("Buffer full")
      sender() ! LineDenied
    case Line(line) =>
      sender() ! LineAccepted
      if (buf.isEmpty && totalDemand > 0)
        onNext(line)
      else {
        buf :+= line
        deliverBuf()
      }
    case Error(t) =>
      onError(t)
    case Request(_) =>
      deliverBuf()
    case Cancel =>
      context.stop(self)
  }

  @tailrec final def deliverBuf(): Unit =
    if (totalDemand > 0) {
      /*
       * totalDemand is a Long and could be larger than
       * what buf.splitAt can accept
       */
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use foreach onNext
      } else {
        val (use, keep) = buf.splitAt(Int.MaxValue)
        buf = keep
        use foreach onNext
        deliverBuf()
      }
    }
}
