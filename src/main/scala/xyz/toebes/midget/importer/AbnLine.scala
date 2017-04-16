package xyz.toebes.midget.importer

import java.io.File
import java.nio.charset.StandardCharsets

import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import xyz.toebes.midget.App
import xyz.toebes.midget.Visualization.Transaction
import xyz.toebes.midget.config.Config
import xyz.toebes.midget.util.Error
import xyz.toebes.midget.Visualization._
import scalaz.\/

object AbnLine {
  implicit val stringDecoder: CellDecoder[String] =
    CellDecoder(s ⇒ DecodeResult(s.toLowerCase().replaceAll("[ ]+", " ")))

  def readLines: \/[Seq[String], Seq[AbnLine]] = {
    val fileContents: String = new File(Config.abn).listFiles.filter(file => file.getName.endsWith("TAB")).map(file => {
      scala.io.Source.fromFile(file.toURI)(StandardCharsets.ISO_8859_1).mkString
    }).mkString(System.lineSeparator())

    Error.handleErrors(fileContents.asCsvReader[AbnLine]('\t', false).toList)
  }

  private def fromPlainString(value: String) = Date(value.substring(0, 4).toInt, value.substring(4, 6).toInt, value.substring(6, 8).toInt)

  def readTransactions: \/[Seq[String], Seq[Transaction]] = {
    readLines.map((items: Seq[AbnLine]) => items.map(item => Transaction(fromPlainString(item.date), item.account, BigDecimal(item.balanceAfter.replace(",", ".")))))
  }
}

case class AbnLine(
  account: String,
  currency: String,
  date: String,
  balanceBefore: String,
  balanceAfter: String,
  date2: String,
  amount: String,
  description: String
)
