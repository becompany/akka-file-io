package ch.becompany.akka.io.csv

import cats._
import cats.data.Validated
import cats.data.Validated._
import cats.data.{NonEmptyList => NEL}
import cats.std.all._
import cats.syntax.cartesian._
import shapeless._
import shapeless.labelled.FieldType
import shapeless.ops.hlist._
import shapeless.ops.record.Keys

trait HListParser[T <: HList] {
  def apply(l: List[(String, String)]): LineResult[T]
}

object HListParser {

  implicit val nelSemigroup: Semigroup[NEL[String]] = SemigroupK[NEL].algebra[String]

  implicit val hnilParser: HListParser[HNil] = new HListParser[HNil] {
    def apply(s: List[(String, String)]): LineResult[HNil] =
      s match {
        case Nil => Valid(HNil)
        case (h, _) +: t => Invalid(NEL(s"""Expected end, got "$h"."""))
      }
  }

  implicit def hconsParser[H, T <: HList](implicit
      headParser: Parser[H],
      tailParser: HListParser[T]): HListParser[H :: T] =
    new HListParser[H :: T] {
      def apply(s: List[(String, String)]): LineResult[H :: T] = s match {
        case Nil => invalid(NEL("Excepted list element."))
        case (h, label) +: t =>
          (headParser(h).leftMap(s"[$label] " + _).toValidatedNel |@| tailParser(t)) map { _ :: _ }
      }
    }

}

trait LineParser[T] {
  def apply(l: List[String]): LineResult[T]
}

object LineParser {

  object toName extends Poly1 {
    implicit def keyToName[A] = at[Symbol with A](_.name)
  }

  implicit def caseClassParser[A, R <: HList, LR <: HList, K <: HList, KL <: HList](implicit
      gen: Generic.Aux[A, R],
      lgen: LabelledGeneric.Aux[A, LR],
      reprParser: HListParser[R],
      keys: Keys.Aux[LR, K],
      mapper: Mapper.Aux[toName.type, K, KL],
      toList: ToTraversable.Aux[KL, List, String]): LineParser[A] =
    new LineParser[A] {
      def apply(record: List[String]): LineResult[A] = {
        val keyList = keys.apply().map(toName).toList
        val pairs = record.zipAll(keyList, "", "").take(record.size)
        reprParser(pairs).map(gen.from _)
      }
    }

  def apply[A](s: List[String])(implicit parser: LineParser[A]): LineResult[A] = parser(s)
}