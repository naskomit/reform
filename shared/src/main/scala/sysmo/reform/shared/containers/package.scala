package sysmo.reform.shared

import cats.MonadThrow
import sysmo.reform.shared.util.Injector

import scala.concurrent.{ExecutionContext, Future}

package object containers {
  def catch_exception[T, F[+_]](expr: => F[T])(implicit mt: MonadThrow[F]): F[T] = {
    try {
      expr
    } catch {
      case err: Throwable => mt.raiseError(err)
    }
  }
}
