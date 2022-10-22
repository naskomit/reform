package sysmo.reform.shared.storage

import cats.MonadThrow
import sysmo.reform.shared.runtime.RecordObject
import sysmo.reform.shared.types.RecordType

trait Storage[F[+_]] {
  implicit val mt: MonadThrow[F]
  def session: StorageSession[F]
}

trait CRUDService[F[+_]] {
  implicit val mt: MonadThrow[F]
  def insert(obj: RecordObject[F]): F[Unit]
}

trait StorageSession[F[+_]] {
  implicit val mt: MonadThrow[F]
  def schema: SchemaService[F]
  def with_transaction(f: => F[Unit]): F[Unit]
}