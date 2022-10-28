package sysmo.reform.storage.orientdb

import cats.MonadThrow

object Util {
  def catch_exception[T, F[+_]](expr: => T)(implicit mt: MonadThrow[F]): F[T] = {
    try {
      mt.pure(expr)
    } catch {
      case err: Throwable => mt.raiseError(err)
    }
  }

}
