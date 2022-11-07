package sysmo.reform.shared.util

import scala.concurrent.Future

object containers {
  type FLocal[+T] = Either[Throwable, T]
  object FLocal {
    def apply[T](x: T): containers.FLocal[T] = Right(x)
    def error[T](e: Throwable): containers.FLocal[T] = Left(e)
    def from_option[T](x: Option[T]): containers.FLocal[T] = x match {
      case Some(value) => Right(value)
      case None => Left(new IllegalArgumentException("No value"))
    }
  }

  type FRemote[+T] = Future[T]
  object FRemote {
    def apply[T](x: T): containers.FRemote[T] = Future.successful(x)
    def error[T](e: Throwable): containers.FRemote[T] = Future.failed(e)
  }

  trait ContainerConversions[T] {
    def to_future: Future[T]
  }

  object implicits {
    implicit class FLocalConversions[T](x: FLocal[T]) extends ContainerConversions[T] {
      override def to_future: FRemote[T] = x match {
        case Left(error) => Future.failed(error)
        case Right(value) => Future.successful(value)
      }
    }
  }

}
