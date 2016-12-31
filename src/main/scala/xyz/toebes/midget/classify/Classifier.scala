package xyz.toebes.midget.classify

import kantan.codecs.Result
import kantan.csv.ReadError
import xyz.toebes.midget.output.Settings
import xyz.toebes.midget.parse.{ParsedLine, Parser}

import scalaz.\/

object Classifier {

  trait ClassifyError extends Product with Serializable
  case class NoClassifyError(item: ParsedLine) extends ClassifyError {

  }

  case class MultipleClassifyError(item: ParsedLine, matchedRules: Seq[ClassifyRule]) extends ClassifyError {
    override def toString =
      (Seq(item.toString) ++ matchedRules.map(rule => rule.toString)).mkString(System.lineSeparator())
  }

  type ClassifyResult[A] = Result[MultipleClassifyError, A]

  def classify(items: Seq[ParsedLine], rules: Seq[ClassifyRule], settings: Settings): ClassifiedLines = {
    ClassifiedLines(
      items.map(item => {
        val matchedRules = rules.filter(rule =>
          if (!rule.hasSecondFilter) {
            item.getCCParams.getOrElse(rule.field, "").toString.toLowerCase().contains(rule.filter.toLowerCase())
          } else {
            item.getCCParams.getOrElse(rule.field, "").toString.toLowerCase().contains(rule.filter.toLowerCase()) &&
              item.getCCParams.getOrElse(rule.field2.get, "").toString.toLowerCase().contains(rule.filter2.get.toLowerCase())
          })

        ClassifiedLine(matchedRules, item)
      }),
      settings
    )
  }

  def unusedRules(items: Seq[ParsedLine], rules: Seq[ClassifyRule], settings: Settings): \/[Seq[String], Unit] = {
    import scalaz._

    val unmatchedRules = rules.filterNot(rule =>
      if (!rule.hasSecondFilter) {
        items.exists(item => item.getCCParams.getOrElse(rule.field, "").toString.toLowerCase().contains(rule.filter.toLowerCase()))
      } else {
        items.exists(item => item.getCCParams.getOrElse(rule.field, "").toString.toLowerCase().contains(rule.filter.toLowerCase()) &&
          item.getCCParams.getOrElse(rule.field2.get, "").toString.toLowerCase().contains(rule.filter2.get.toLowerCase()))
      })

    if (unmatchedRules.isEmpty)
      \/.right(Unit)
    else
      \/.left(unmatchedRules.map(_.toString))
  }
}
