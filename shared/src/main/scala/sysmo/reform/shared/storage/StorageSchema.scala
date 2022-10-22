package sysmo.reform.shared.storage

import sysmo.reform.shared.types.RecordType

trait StorageSchema {
  def name: String
}

trait SchemaService[F[+_]] {
  def list_schemas: Iterable[StorageSchema]
  def add_record_schema(dtype: RecordType): F[Unit]
//  def drop_record_schema(dtype: RecordType): F[Unit]
//  def ensure_record_class(dtype: RecordType): F[Unit]
}
