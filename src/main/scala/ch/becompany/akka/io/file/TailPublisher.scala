package ch.becompany.akka.io.file

import java.nio.file.Path

import akka.actor.{ActorRef, Props}
import akka.stream.actor.ActorPublisher
import akka.util.{ByteString, Timeout}
import org.apache.commons.io.input.{TailerListenerAdapter, Tailer => CommonsTailer}

import scala.annotation.tailrec
import scala.concurrent.duration._

class Tailer(val path: Path, val encoding: String, val end: Boolean, val publisher: ActorRef)
  extends TailerListenerAdapter {

  import TailPublisher._
  implicit val timeout = Timeout(1 second)

  override def handle(line: String): Unit =
    publisher ! Line(ByteString(line, encoding))

  override def handle(e: Exception): Unit =
    publisher ! Error(e)

  def run(): Unit =
    CommonsTailer.create(path.toFile, this, 1000L, end)

}

object TailPublisher {
  def props: Props = Props[TailPublisher]

  case class Line(l: ByteString)
  case class Error(t: Throwable)

  case object LineAccepted
  case class LineDenied(reason: String)

}

class TailPublisher extends ActorPublisher[ByteString] {

  import TailPublisher._
  import akka.stream.actor.ActorPublisherMessage._

  private val MaxBufferSize = 1000
  private var buf = Vector.empty[ByteString]

  private def emitLine(line: ByteString) = {
    sender() ! LineAccepted
    if (buf.isEmpty && totalDemand > 0)
      onNext(line)
    else {
      buf :+= line
      emitBuffer()
    }
  }

  def receive = {
    case Line(_) if buf.size == MaxBufferSize => sender() ! LineDenied("Buffer full")
    case Line(line) => emitLine(line)
    case Error(t) => onError(t)
    case Request(_) => emitBuffer()
    case Cancel => context.stop(self)
  }

  @tailrec private final def emitBuffer(): Unit =
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
        emitBuffer()
      }
    }
}