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
    Validated.fromTry(Try(parse(s))).leftMap(t => s"${t.getClass} ${t.getMessage}")
}

object Parsers {

  implicit def optionParser[T](implicit parser: Parser[T]): Parser[Option[T]] =
    new Parser[Option[T]] {
      def apply(s: String) = s match {
        case "" => valid(None)
        case t => parser(t).map(Some(_))
      }
    }

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