package xyz.toebes.midget.output

import com.github.sh0hei.mascalade.tableflip.TableFlip

case class TableOutput(header: Seq[String], data: Seq[Seq[String]]) {
  def getOutput = TableFlip.of(header.toArray, data.map(_.toArray).toArray)

  def getOutputFilter = {
    val patchH = header.patch(1, Nil, 1).patch(2, Nil, 1)
    val patchD = data
      .map(_.patch(1, Nil, 1).patch(2, Nil, 1))
      .map(_.zipWithIndex.map(item => {
        item._2 match {
          case 2 => item._1.substring(0, Math.min(item._1.length, 26))
          case 3 => item._1.substring(0, Math.min(item._1.length, 80))
          case _ => item._1
        }
      }))

    TableFlip.of(patchH.toArray, patchD.map(_.toArray).toArray)
  }
}

