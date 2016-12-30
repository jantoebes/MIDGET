package xyz.toebes.midget.util

import kantan.codecs.Result

import scala.reflect.ClassTag
import scalaz._, Scalaz._
object Error {
  type OptionalError[T] = \/[Seq[String], T]

  def handleErrors[T: ClassTag](items: Seq[Result[_, T]]): \/[Seq[String], Seq[T]] = {
    val errors: Seq[_] = items.collect { case kantan.codecs.Result.Failure(a) ⇒ a }
    val success: Seq[T] = items.collect { case kantan.codecs.Result.Success(a) ⇒ a }

    if (errors.nonEmpty) {
      errors.map(_.toString).left[Seq[T]]
    } else {
      success.toSeq.right[Seq[String]]
    }

  }

}
