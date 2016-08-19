package ch.becompany.akka.io

import cats.data.{Validated, _}

package object csv {

  type LineResult[T] = Validated[NonEmptyList[String], T]

}
