package xyz.toebes.midget.parse

import xyz.toebes.midget.importer.AbnLine

import scala.reflect.runtime.universe._

object Parser {
  object SimpleSepaFields extends Enumeration {
    type SimpleDescriptionFieldType = Value

    val base, iban, naam, bic, omschrijving, machtiging, kenmerk, incassant = Value
  }

  object SlashSepaFields extends Enumeration {
    type SlashDescriptionFieldType = Value

    val csid, name, marf, remi, iban, bic, eref = Value
  }

  def parse(items: Seq[AbnLine]): Seq[ParsedLine] = {

    items.map(_ match {
      case it if it.description.startsWith("sepa") => {
        val map = parseSimpleSepa(it.description)
        ParsedLine(it.date.take(6), it.amount.replace(",", ".").toDouble, get(map, SimpleSepaFields.iban), get(map, SimpleSepaFields.naam), get(map, SimpleSepaFields.omschrijving))
      }
      case it if it.description.startsWith("/trtp") => {
        val map = parseSlashSepa(it.description)
        ParsedLine(it.date.take(6), it.amount.replace(",", ".").toDouble, get(map, SlashSepaFields.iban), get(map, SlashSepaFields.name), get(map, SlashSepaFields.remi))
      }

      case it => ParsedLine(it.date.take(6), it.amount.replace(",", ".").toDouble, "", "", it.description)
    })
  }

  private def get(map: Map[String, String], key: Any) = map.getOrElse(key.toString, "")

  private def parseSimpleSepa(description: String): Map[String, String] = {
    val items: Seq[String] = SimpleSepaFields.values
      .foldLeft(Seq(SimpleSepaFields.base + ":" + description))((result, key) => result.flatMap(_.split("""(?=""" + key + """:)""")))

    items.map(it => {
      val splited = it.split(":")

      splited.head.trim -> splited.tail.mkString(":").trim
    }).toMap
  }

  private def parseSlashSepa(description: String): Map[String, String] = {
    SlashSepaFields.values.flatMap(key =>
      ("""\/""" + key + """\/(.*?)\/""").r.findFirstMatchIn(description).map(g => (key.toString -> g.group(1)))).toMap
  }
}
