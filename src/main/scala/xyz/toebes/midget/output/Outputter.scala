package xyz.toebes.midget.output

import java.io._

import io.github.cloudify.scala.spdf
import io.github.cloudify.scala.spdf._
import xyz.toebes.midget.config.Config

object Outputter {
  def printText(folder: String, name: String, content: String, ext: String = "txt") = {
    import java.io._
    createFolder(Config.output + folder)
    val pw = new PrintWriter(new File(Config.output + folder + "/" + name + "." + ext))
    try {
      pw.write(content)
    } finally {
      pw.close
    }
  }

  def printPdf(folder: String, name: String, _content: String, large: Boolean, fontSize: Option[Int] = None) = {
    createFolder(Config.output + folder)

    val content = _content.replace(" ", "&nbsp;").replace(System.lineSeparator(), "<br>")

    val pdf = Pdf(new PdfConfig {
      orientation := (if (large) spdf.Landscape else Portrait)
      pageSize := "A4"
      marginTop := "0.75in"
      marginBottom := "0.75in"
      marginLeft := "0.75in"
      marginRight := "0.75in"

    })

    val outputStream = new ByteArrayOutputStream
    try {
      val size = fontSize.getOrElse(if (large) 16 else 7)
      pdf.run(html(name, content, size), outputStream)
      outputStream.writeTo(new FileOutputStream(Config.output + folder + "/" + name + ".pdf"))
    } finally {
      outputStream.close()
    }
  }

  def html(name: String, value: String, fontSize: Int) = {

    s"""<html style="font-family:'Courier New';font-size:${fontSize}px"><h1 align="center">$name</h1>$value</html>"""
  }

  def createFolder(path: String): Unit = {
    val dir = new File(path)
    if (!dir.exists()) dir.mkdir()
  }
}
