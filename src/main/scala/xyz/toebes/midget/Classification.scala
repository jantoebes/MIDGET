package xyz.toebes.midget

import xyz.toebes.midget.classify.{ ClassifiedLine, Classifier, ClassifyRule, _ }
import xyz.toebes.midget.group.GroupReport
import xyz.toebes.midget.importer.AbnLine
import xyz.toebes.midget.output.{ Outputter, Settings, TableOutput }
import xyz.toebes.midget.parse.{ ParsedLine, Parser }

import scalaz.\/

object Classification {
  def classify = {
    val result: \/[Seq[String], (ClassifiedLines, GroupReport, Settings)] = for {
      classifyRules <- ClassifyRule.readClassifyRules
      csvLines <- AbnLine.readLines
      settings <- Settings.read
      ignores <- ClassifyRule.readCategoryIgnores
      parsedItems: Seq[ParsedLine] = Parser.parse(csvLines)
      _ <- Classifier.unusedRules(parsedItems, classifyRules, settings)
    } yield {
      val classifiedLines: ClassifiedLines = Classifier.classify(parsedItems, classifyRules, settings)

      val groupReport: GroupReport = GroupReport(classifiedLines.getSuccess, ignores, settings)

      (classifiedLines, groupReport, settings)
    }

    result.bimap(
      printErrors,
      {
        case (classifiedLines, groupReport, settings) => {
          printErrors(Seq.empty)
          printClassifiedLinesFailed(classifiedLines)
          printClassifiedLinesSuccess(classifiedLines)
          printMonthReport(classifiedLines, settings.printPdf)
          printGroupReport(groupReport, settings.printPdf)
          printUitgaven(groupReport, settings.printPdf)
        }
      }
    )
  }

  def printErrors(errors: Seq[String]) = {
    Outputter.printText("errors", "error", TableOutput(Seq("error"), errors.map(Seq(_))).getOutput)
  }

  def printClassifiedLinesFailed(classifiedLines: ClassifiedLines) =
    Outputter.printText("errors", "classifyfailed", TableOutput(
      ClassifiedLine.getHeader,
      classifiedLines.getFailure.map(_.toTableRow(false))
    ).getOutput)

  def printClassifiedLinesSuccess(classifiedLines: ClassifiedLines) =
    Outputter.printText("errors", "classifyresult", TableOutput(
      ClassifiedLine.getHeader,
      classifiedLines.getSuccess.map(_.toTableRow(true))
    ).getOutput)

  def printGroupReport(groupReport: GroupReport, printPdf: Boolean) = {
    Outputter.printText("output", "common", groupReport.toTable.getOutput)

    if (printPdf) {
      Outputter.printPdf("pdf", "Inkomsten_Uitgaven", groupReport.toTable.getOutput, true, Some(8))
    }
  }

  def printMonthReport(classifiedLines: ClassifiedLines, printPdf: Boolean) =
    classifiedLines.getSuccess
      .groupBy(_.groupDate)
      .foreach(item => {
        Outputter.printText("output/", item._1, TableOutput(
          ClassifiedLine.getHeader,
          item._2.map(_.toTableRow(true))
        ).getOutputFilter)

        if (printPdf)
          Outputter.printPdf("pdf/", item._1, TableOutput(
            ClassifiedLine.getHeader,
            item._2.filter(_.category.startsWith("uitgaven_")).map(_.toTableRow(true, true))
          ).getOutputFilter, false)
      })

  def printUitgaven(groupReport: GroupReport, printPdf: Boolean) = {
    Outputter.printText("output", "uitgaven", groupReport.toDailyTable.getOutput)

    if (printPdf)
      Outputter.printPdf("pdf", "Uitgaven", groupReport.toDailyTable.getOutput, true, Some(8))
  }

  def parse(raw: Seq[AbnLine]) = Parser.parse(raw)
}
