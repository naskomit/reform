package sysmo.reform.shared.runtime

import cats.MonadThrow
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.types.{ArrayType, RecordType}
//import sysmo.reform.shared.containers.{FLocal, FRemote}
import cats.syntax.all._
import sysmo.reform.shared.query.Query
import sysmo.reform.shared.service.RemoteService
import sysmo.reform.shared.table.{LocalTable, Table}
import sysmo.reform.shared.table.Table.Schema
import sysmo.reform.shared.types.TypeSystem
import sysmo.reform.shared.util.MonadicIterator


class RemoteMethods[F[+_]](service: RemoteService[F]) {
  import sysmo.reform.shared.data.Transport._
  import sysmo.reform.shared.table.Transport._
  import sysmo.reform.shared.types.Transport._
  import sysmo.reform.shared.query.Transport._

  implicit val mt: MonadThrow[F] = service.mt

  def query(q: Query): F[Table[F]] =
    service.do_call[Query, LocalTable]("query", q)
    .map(local_table => new Table[F] {
      override implicit val mt: MonadThrow[F] = service.mt
      override def schema: Schema = local_table.schema
      override def nrow: F[Int] = local_table.nrow.to[F]
      override def row_iter: MonadicIterator[F, Table.Row] =
        MonadicIterator.from_iterator[F, Table.Row](local_table.iterator)
      override def cache: F[LocalTable] = mt.pure(local_table)
    })
}

class RemoteConstructors[F[+_]](implicit mt: MonadThrow[F]) extends Constructors[F] {
  override def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordInstance[F]] =
    mt.pure(LocalObjects.record(dtype, id, parent)(mt))
  override def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayInstance[F]] =
    mt.pure(LocalObjects.array(dtype, id, parent)(mt))
}

class RemoteRuntime[_F[+_]](val type_system: TypeSystem, val methods: RemoteMethods[_F])
  extends RFRuntime[_F]{
  implicit val mt: MonadThrow[F] = methods.mt
  override implicit val constructors: Constructors[F] =
    new RemoteConstructors[F]

  override def list: MonadicIterator[F, ObjectProxy] = ???

  override def count: F[Int] = ???

  override def count(q: Query): F[Int] = ???

  override def run_query(q: Query): F[Table[F]] = methods.query(q)

  override def dispatch(action: RuntimeAction): F[Unit] = ???

  override def get(id: ObjectId): F[RTO] = ???

  override def create_record(dtype: Schema, parent: Option[ObjectId]): F[RecordInstance[F]] = ???

  override def create_array(dtype: ArrayType, parent: Option[ObjectId]): F[ArrayInstance[F]] = ???

  override def update_record(id: ObjectId, field_values: Seq[(String, Value)]): F[RecordInstance[F]] = ???

  override def remove(id: ObjectId): F[Unit] = ???
}

object RemoteRuntime {
  def apply[F[+_]](type_system: TypeSystem, service: RemoteService[F]): RemoteRuntime[F] = {
    val methods = new RemoteMethods(service)
    new RemoteRuntime(type_system, methods)
  }
}