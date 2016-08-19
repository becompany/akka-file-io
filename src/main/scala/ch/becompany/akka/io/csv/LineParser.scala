package ch.becompany.akka.io.csv

import cats._
import cats.data.Validated
import cats.data.Validated._
import cats.data.{NonEmptyList => NEL}
import cats.std.all._
import cats.syntax.cartesian._
import shapeless.{::, Generic, HList, HNil}

trait LineParser[T] {
  def apply(l: List[String]): LineResult[T]
}

object LineParser {

  implicit val nelSemigroup: Semigroup[NEL[String]] = SemigroupK[NEL].algebra[String]

  implicit val hnilParser: LineParser[HNil] = new LineParser[HNil] {
    def apply(s: List[String]): LineResult[HNil] =
      s match {
        case Nil => Valid(HNil)
        case h +: t => Invalid(NEL(s"""Expected end, got "$h"."""))
      }
  }

  implicit def hconsParser[H : Parser, T <: HList : LineParser]: LineParser[H :: T] =
    new LineParser[H :: T] {
      def apply(s: List[String]): LineResult[H :: T] = s match {
        case Nil => invalid(NEL("Excepted list element."))
        case h +: t =>
          val head = implicitly[Parser[H]].apply(h).toValidatedNel
          val tail = implicitly[LineParser[T]].apply(t)
          (head |@| tail) map { _ :: _ }
      }
    }

  implicit def caseClassParser[A, R <: HList]
  (implicit gen: Generic[A] { type Repr = R }, reprParser: LineParser[R]): LineParser[A] =
    new LineParser[A] {
      def apply(s: List[String]): LineResult[A] =
        reprParser.apply(s).map(gen.from)
    }

  def apply[A](s: List[String])(implicit parser: LineParser[A]): LineResult[A] = parser(s)
}