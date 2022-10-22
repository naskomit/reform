package sysmo.reform

import cats.MonadThrow
import com.typesafe.config.Config

package object storage {
  def create_orientdb[F[+_]](config: Config)(implicit mt: MonadThrow[F]): orientdb.StorageImpl[F] = {
    new orientdb.StorageImpl(config)
  }
}