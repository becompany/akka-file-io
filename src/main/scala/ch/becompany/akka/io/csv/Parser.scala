package ch.becompany.akka.io.csv

import scala.util.{Success, Try}
import cats.data.Validated
import cats.data.Validated.{invalid, valid}

trait Parser[T] {
  def apply(s: String): Validated[String, T]
}

trait TryParser[T] extends Parser[T] {

  def parse(s: String): T

  def apply(s: String): Validated[String, T] =
    Try(parse(s)).transform(
      s => Success(valid(s)),
      f => Success(invalid(f.getMessage))).get
}

object Parsers {

  implicit val stringParser: Parser[String] = new Parser[String] {
    def apply(s: String) = valid(s)
  }

  implicit val intParser: Parser[Int] = new TryParser[Int] {
    def parse(s: String): Int = s.toInt
  }

  implicit val longParser: Parser[Long] = new TryParser[Long] {
    def parse(s: String): Long = s.toLong
  }

}