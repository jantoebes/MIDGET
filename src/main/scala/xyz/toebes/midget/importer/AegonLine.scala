package xyz.toebes.midget.importer

import java.io.{File, StringWriter}
import java.nio.charset.StandardCharsets

import kantan.codecs.Result
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import org.apache.commons.cli.{CommandLine, GnuParser}
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import technology.tabula.CommandLineApp
import xyz.toebes.midget.Visualization._
import xyz.toebes.midget.config.Config
import xyz.toebes.midget.util.Error

import scalaz.\/

object AegonLine {
  private def parsePdf(args: Array[String]) = {
    val parser: GnuParser = new GnuParser

    val commandLine = parser.parse(CommandLineApp.buildOptions, args)

    val writer = new StringWriter()

    new CommandLineApp(writer).extractTables(commandLine)

    writer.toString
  }

  private def getPdfTableContent(fileName: String) = parsePdf(Array("-a 125,53,761,1074", fileName))
  private def getRekeningNummer(fileName: String) = {
    val regex = ",([0-9]+)".r
    val items = parsePdf(Array(fileName)).split("\r\n")
    val pdfContent = items.find(item => item.contains("Rekeningnummer")).get

    regex.findAllIn(pdfContent).matchData.toSeq.head.group(1)
  }

  def readLines(): \/[Seq[String], Seq[AegonLine]] = {

    val result = new File(Config.aegon).listFiles.filter(file => file.getName.endsWith("pdf")).flatMap(file => {
      val rekeningNummer = getRekeningNummer(Config.aegon + file.getName)

      val rawPdfTableContent = getPdfTableContent(Config.aegon + file.getName)

      val rawLines = rawPdfTableContent
        .replaceAll("€","")
        .replaceAll("\" ", "\"")
        .replaceAll(" \"", "\"")

      rawLines.asCsvReader[AegonLinePdf](',', true).toList.map(items => items.map(item => AegonLine(item.datum, rekeningNummer, item.saldo)))
    })

    Error.handleErrors(result)
  }

  private def fromPlainString(value: String) = Date(value.substring(6, 10).toInt, value.substring(3, 5).toInt, value.substring(0, 2).toInt)

  def readTransactions: \/[Seq[String], Seq[Transaction]] = {
    val lines = readLines

    lines.map((items: Seq[AegonLine]) => items.map(item=>Transaction(fromPlainString(item.datum), item.account, BigDecimal(item.saldo.replace(".", "").replace(",", ".")))))
  }
}

case class AegonLinePdf(
                    datum: String,
                    omschrijving: String,
                    tenaamstelling: String,
                    tegenrekening: String,
                    unknown: String,
                    bedrag: String,
                    saldo: String
                    )

case class AegonLine(
                         datum: String,
                         account: String,
                         saldo: String
                       )