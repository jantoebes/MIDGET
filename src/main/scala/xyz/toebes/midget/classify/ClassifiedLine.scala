package xyz.toebes.midget.classify

import pl.project13.scala.rainbow.Rainbow
import xyz.toebes.midget.parse.{ ParsedLine, Parser }
import Rainbow._

object ClassifiedLine {
  def getHeader: Seq[String] = {
    Seq("category") ++ ParsedLine.getHeader
  }
}

case class ClassifiedLine(rules: Seq[ClassifyRule], line: ParsedLine) {
  def isValid: Boolean = rules.size == 1

  def toTableRow(simple: Boolean, withoutUitgaven: Boolean = false): Seq[String] = {
    // Map category to 1 cell
    val x = if (simple) {
      rules
        .map(_.category)
        .map(text => if (withoutUitgaven) text.replace("uitgaven_", "") else text).mkString(System.lineSeparator())
    } else {
      rules.mkString(System.lineSeparator())
    }
    Seq(x) ++ line.toTable
  }

  def rule = {
    rules.headOption
  }

  def category = {
    rule.map(_.category).getOrElse("")
  }
  def groupDate = {
    line.date.take(6)
  }

}
