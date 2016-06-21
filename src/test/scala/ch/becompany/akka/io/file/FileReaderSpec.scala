package ch.becompany.akka.io.file

import java.nio.charset.StandardCharsets
import java.nio.file._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class FileReaderSpec extends FlatSpec with Matchers with ScalaFutures {

  implicit val system = ActorSystem("akka-file-io")
  implicit val materializer = ActorMaterializer()

  "File reader" should "continuously read a file" in {
    val f = Files.createTempFile(null, ".log")

    def writeAsync(lines: String*): Unit = {
      new Thread() {
        override def run(): Unit = {
          Thread.sleep(10)
          Files.write(f, lines.asJava, StandardCharsets.UTF_8, StandardOpenOption.APPEND)
          println(s"Wrote $lines")
        }
      }.start()
    }

    val src = FileReader.readContinuously(f.toString, false, Some("UTF-8"))
    val probe = src.runWith(TestSink.probe[String])
    Thread.sleep(500)

    writeAsync("foo", "bar")
    probe.
      request(1).
      expectNext("foo")

    writeAsync("baz")
    probe.
      request(2).
      expectNext("bar", "baz")
  }

}
