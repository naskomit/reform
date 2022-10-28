package sysmo.reform.shared.util

import scala.concurrent.Future

object containers {
  type FLocal[+T] = Either[Throwable, T]
  object FLocal {
    def apply[T](x: T): containers.FLocal[T] = Right(x)
    def error[T](e: Throwable): containers.FLocal[T] = Left(e)
  }

  type FRemote[+T] = Future[T]
  object FRemote {
    def apply[T](x: T): containers.FRemote[T] = Future.successful(x)
    def error[T](e: Throwable): containers.FRemote[T] = Future.failed(e)
  }

}
