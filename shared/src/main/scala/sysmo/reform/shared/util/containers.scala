package sysmo.reform.shared.util

object containers {
  type FLocal[+T] = Either[Throwable, T]
  object FLocal {
    def apply[T](x: T): containers.FLocal[T] = Right(x)
    def error[T](e: Throwable): containers.FLocal[T] = Left(e)
  }
}
