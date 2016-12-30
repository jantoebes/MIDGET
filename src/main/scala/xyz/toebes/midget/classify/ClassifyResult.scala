package xyz.toebes.midget.classify

import xyz.toebes.midget.output.Settings

case class ClassifiedLines(classifies: Seq[ClassifiedLine], settings: Settings) {

  val preSort = settings.sortBy match {
    case "c" => classifies.sortBy(i => (i.category, i.line.date, i.line.amount))
    case "d" => classifies.sortBy(i => (i.line.date, i.category, i.line.amount))
    case "a" => classifies.sortBy(i => (i.line.date, i.line.amount))
    case "o" => classifies.sortBy(_.line.omschrijving)
    case _   => classifies
  }

  val sort = if (!settings.asc) preSort.reverse else preSort

  def isSuccess = getFailure.isEmpty

  def getFailure = sort.filter(!_.isValid)

  def getSuccess = if (isSuccess) sort else Seq.empty
}
