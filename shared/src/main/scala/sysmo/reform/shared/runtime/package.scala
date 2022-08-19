package sysmo.reform.shared

package object runtime {
  type FLocal[+T] = Either[Throwable, T]
  object FLocal {
    def apply[T](x: T): FLocal[T] = Right(x)
    def error[T](e: Throwable): FLocal[T] = Left(e)
  }
}
