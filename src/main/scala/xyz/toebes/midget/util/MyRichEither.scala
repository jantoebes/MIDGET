package xyz.toebes.midget.util

import xyz.toebes.midget.FilesToWrite

object MyRichEither {
  implicit def richInt[T](either: Either[Seq[String], Seq[T]]) = new MyRichEither(either)
}

class MyRichEither[T](either: Either[Seq[String], Seq[T]]) {
  def getFilesToWrite(i: Seq[T] => FilesToWrite): FilesToWrite = either match {
    case Left(value)  => FilesToWrite(value)
    case Right(value) => i(value)
  }
}

