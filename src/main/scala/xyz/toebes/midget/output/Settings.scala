package xyz.toebes.midget.output

import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import xyz.toebes.midget.App
import xyz.toebes.midget.config.Config
import xyz.toebes.midget.parse.{ParsedLine, Parser}
import xyz.toebes.midget.util.Error._

object Settings {
  def read: OptionalError[Settings] = {
    val file = scala.io.Source.fromFile(Config.settings)
    val content = file.mkString
    handleErrors(content.mkString.asCsvReader[Settings](',', true).toList).map(_.head)
  }
}

case class Settings(sortBy: String, asc: Boolean, groupSortBy: String, groupSortByAsc: Boolean, showGroup: Boolean, printPdf: Boolean)