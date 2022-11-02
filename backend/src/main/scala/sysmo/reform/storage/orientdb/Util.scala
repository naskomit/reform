package sysmo.reform.storage.orientdb

import cats.MonadThrow
import sysmo.reform.shared.data.ObjectId

object Util {
  def catch_exception[T, F[+_]](expr: => F[T])(implicit mt: MonadThrow[F]): F[T] = {
    try {
      expr
    } catch {
      case err: Throwable => mt.raiseError(err)
    }
  }

  def ensure_orientid[F[+_]](id: ObjectId)(implicit mt: MonadThrow[F]): F[OrientId] = {
    id match {
      case oid: OrientId => mt.pure(oid)
      case _ => mt.raiseError(new IllegalArgumentException(s"Not an OrientId: ${id}"))
    }
  }
}
