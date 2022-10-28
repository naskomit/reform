package sysmo.reform.shared.storage

import sysmo.reform.shared.types.{RecordType, TypeSystem}

trait StorageSchema {
  def name: String
}

trait SchemaService[F[+_]] {
  def list_schemas: F[Iterable[StorageSchema]]
  def has_schema(name: String): F[Boolean]
  def ensure_schema(name: String): F[Unit]
  //  def ensure_base_schemas(): F[Unit]
  def add_record_schema(record_type: RecordType): F[Unit]
  //  def drop_record_schema(record_type: RecordType): F[Unit]
  def ensure_record_schema(record_type: RecordType): F[Unit]
  def sync(type_system: TypeSystem): F[Unit]
}
