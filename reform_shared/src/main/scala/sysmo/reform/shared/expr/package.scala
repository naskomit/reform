package sysmo.reform.shared

import scala.reflect.ClassTag

package object expr {
  type Result[T] = Either[EvalError, T]
  def as[T](x : Result[_])(implicit ev: ClassTag[T]): Result[T] = x.flatMap{
    case v: T => Right(v)
    case x => Left(IncorrectTypeError(x, ev.getClass.getName))
  }

}
