package sysmo.reform.storage.orientdb

import cats.MonadThrow
import sysmo.reform.shared.data.ObjectId

object Util {
  def ensure_orientid[F[+_]](id: ObjectId)(implicit mt: MonadThrow[F]): F[OrientId] = {
    id match {
      case oid: OrientId => mt.pure(oid)
      case _ => mt.raiseError(new IllegalArgumentException(s"Not an OrientId: ${id}"))
    }
  }
}
