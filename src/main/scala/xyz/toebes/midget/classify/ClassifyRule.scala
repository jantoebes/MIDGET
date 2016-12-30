package xyz.toebes.midget.classify

import kantan.codecs.Result
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import xyz.toebes.midget.config.Config
import xyz.toebes.midget.util.Error
import xyz.toebes.midget.{ App, BaseApp }
import xyz.toebes.midget.parse.{ ParsedLine, Parser }
import scalaz._, Scalaz._
import scala.reflect.ClassTag

object ClassifyRule {
  def content = {
    val file = scala.io.Source.fromFile(Config.rules)
    file.getLines().toSeq
  }

  implicit val stringDecoder: CellDecoder[String] =
    CellDecoder(s ⇒ DecodeResult(s.trim))

  def readClassifyRules: \/[Seq[String], Seq[ClassifyRule]] = {
    val items = content.filter(_.startsWith("c,")).map(_.drop(2)).mkString(System.lineSeparator())

    val result = items.asCsvReader[ClassifyRule](',', false).toList

    Error.handleErrors(result)
  }

  def readCategoryIgnores: \/[Seq[String], Seq[CategoryIgnore]] = {
    val items = content.filter(_.startsWith("i,")).map(_.drop(2)).mkString(System.lineSeparator())
    Error.handleErrors(items.asCsvReader[CategoryIgnore](',', false).toList)
  }

}

case class ClassifyRule(
    category: String,
    field: String,
    filter: String,
    field2: Option[String] = None,
    filter2: Option[String] = None
) {
  require(ParsedLine.getHeader.contains(field), s"$field doesn't exist")
  field2.foreach(it => require(ParsedLine.getHeader.contains(it), s"$it doesn't exist"))

  def hasSecondFilter = field2.isDefined && filter2.isDefined

  override def toString = {
    val base = category.toString + "," + field + "," + filter
    if (hasSecondFilter) base + System.lineSeparator() + "," + field2 + "," + filter2 else base
  }
}

case class CategoryIgnore(category: String)