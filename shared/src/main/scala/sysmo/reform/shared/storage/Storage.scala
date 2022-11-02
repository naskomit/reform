package sysmo.reform.shared.storage

import cats.MonadThrow
import sysmo.reform.shared.data.RecordFieldCodec
import sysmo.reform.shared.query.QueryService
import sysmo.reform.shared.runtime.RFRuntime
import sysmo.reform.shared.types.TypeSystem

trait Storage[F[+_]] {
  implicit val mt: MonadThrow[F]
  def session: StorageSession[F]
}


trait StorageSession[F[+_]] {
  implicit val mt: MonadThrow[F]
  type FieldCodec <: RecordFieldCodec[F]
  val rec_field_codec: FieldCodec
  type QService <: QueryService[F]
  val query_service: QService
  val schema: SchemaService[F]
  def runtime(type_system: TypeSystem): RFRuntime[F]
  def with_transaction(f: => F[Unit]): F[Unit]
}