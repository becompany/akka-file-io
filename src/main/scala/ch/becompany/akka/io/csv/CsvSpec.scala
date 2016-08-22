package ch.becompany.akka.io.csv

import scala.util.matching.Regex

/**
  *
  * @param lineDelimiter Line delimiter string.
  * @param fieldDelimiter Field delimiter regex.
  * @param quote Quote character.
  */
case class CsvSpec(
  lineDelimiter: String = "\n",
  fieldDelimiter: Char = ',',
  quote: Char = '"'
)
