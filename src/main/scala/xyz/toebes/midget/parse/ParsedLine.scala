package xyz.toebes.midget.parse

import scala.reflect.runtime.universe._

object ParsedLine {
  def getHeader: Seq[String] = {
    typeOf[ParsedLine].members.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }.toSeq.map(_.name.toString).reverse

  }
}

case class ParsedLine(date: String, amount: BigDecimal, iban: String, name: String, omschrijving: String) {
  def getCCParams = {
    val values = this.productIterator
    this.getClass.getDeclaredFields.map(_.getName -> values.next).toMap
  }

  def toTable: Seq[String] = {
    val map = getCCParams
    ParsedLine.getHeader.map(it => {
      map(it) match {
        case it: String     => it
        case it: BigDecimal => String.format("%8s", "%.2f".format(it))
      }
    })
  }
}

