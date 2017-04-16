package xyz.toebes.midget

import xyz.toebes.midget.classify.ClassifiedLines
import xyz.toebes.midget.group.GroupReport

case class FilesToWrite(
  error: Seq[String] = Seq.empty,
    classification: Option[ClassifiedLines] = None,
    groupReport: Option[GroupReport] = None
) {

}
