package xyz.toebes.midget.group

import java.text.NumberFormat

import xyz.toebes.midget.classify.{ CategoryIgnore, ClassifiedLine }
import xyz.toebes.midget.group.GroupReport._
import xyz.toebes.midget.output.{ Settings, TableOutput }

import scala.math.BigDecimal.RoundingMode

object GroupReport {
  def roundToString(i: BigDecimal) = {
    val i2 = i.setScale(-1, RoundingMode.HALF_UP).toInt
    val result = String.format("%8s", NumberFormat.getIntegerInstance().format(i2).toString)
    if (result.trim.equalsIgnoreCase("0")) "" else result
  }
}

case class GroupReport(lines: Seq[ClassifiedLine], ignores: Seq[CategoryIgnore], settings: Settings) {
  private val filter = lines.filter(line => !ignores.map(_.category).contains(line.category))
  private val simpleCategories = filter.map(_.category).distinct.sorted
  private def categories(showGroup: Boolean): Seq[String] = simpleCategories.map(i => getGroup(i, showGroup)).distinct

  private def months = filter.map(_.groupDate).distinct.sorted.reverse

  private def groups(showGroup: Boolean): Map[Group, Seq[ClassifiedLine]] = filter.groupBy(record => Group(getGroup(record.category, showGroup), record.groupDate))

  def toTable: TableOutput = Table(
    months,
    categories(settings.showGroup).map(category => {
      val values = months.map(month => {
        val lines = groups(settings.showGroup).getOrElse(Group(category, month), Seq.empty)
        lines.map(_.line.amount).sum
      })
      Row(category, values)
    }), settings
  ).tableOutput

  def toDailyTable: TableOutput = Table(
    months,
    categories(false).filter(_.startsWith("uitgaven_")).map(category => {
      val values = months.map(month => {
        val lines = groups(false).getOrElse(Group(category, month), Seq.empty)
        lines.map(_.line.amount).sum
      })
      Row(category.replace("uitgaven_", ""), values)
    }), Settings("", true, "", true, true, false)
  ).tableOutput

  private def getGroup(i: String, showGroup: Boolean): String = {
    val y = i.lastIndexOf('_')
    if (y != -1 && showGroup) {
      i.splitAt(y)._1
    } else {
      i
    }
  }
}

case class Table(headers: Seq[String], rows: Seq[Row], settings: Settings) {
  def sumrow = "TOTAL"

  private def header = Seq("category") ++ headers ++ Seq("sum")

  private def total: Seq[BigDecimal] = for (i <- 0 to headers.size - 1) yield rows.map(item => item.items(i)).sum

  private def emptyRow = header.map(_ => "")

  private def sumRow: Seq[Seq[String]] = {
    if (rows.length > 0)
      Seq(emptyRow, Seq("TOTAL") ++ total.map(roundToString) ++ Seq(roundToString(total.sum)), emptyRow)
    else
      Seq.empty
  }

  private def normalRow: Seq[Seq[String]] = {
    val sorter1 = settings.groupSortBy match {
      case "c" => rows.sortBy(i => i.category)
      case _ => {
        val (a, b) = rows.partition(_.total <= 0)
        b.sortBy(_.total).reverse ++ a.sortBy(_.total)
      }
    }

    (if (!settings.groupSortByAsc) sorter1.reverse else sorter1).map(_.toRow)
  }

  def tableOutput = TableOutput(header, sumRow ++ normalRow)
}

case class Group(category: String, date: String)

case class Row(category: String, items: Seq[BigDecimal]) {
  def total = items.sum

  def toRow = Seq(
    category
  ) ++
    items.map(roundToString) ++
    Seq(roundToString(total))

}

