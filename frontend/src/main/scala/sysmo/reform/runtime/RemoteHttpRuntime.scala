package sysmo.reform.runtime

import cats.MonadThrow
import cats.implicits._
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.query.{Query, SQLQueryService, SQLQuery}
import sysmo.reform.shared.runtime.LocalRuntimeConstructor.constructors.F
import sysmo.reform.shared.runtime.{ArrayInstance, Constructors, LocalObjects, LocalRuntime, LocalRuntimeConstructor, ObjectProxy, RFRuntime, RecordInstance, RuntimeAction, RuntimeConstructor}
import sysmo.reform.shared.table.Table.Schema
import sysmo.reform.shared.table.{LocalTable, Table}
import sysmo.reform.shared.types.{ArrayType, RecordType, TypeSystem}
import sysmo.reform.shared.util.MonadicIterator
import sysmo.reform.shared.util.containers.FRemote
import sysmo.reform.util.RemoteHttpService

import scala.concurrent.{ExecutionContext, Future}

class RemoteEndpoints(implicit ec: ExecutionContext) extends RemoteHttpService {
  import sysmo.reform.shared.data.Transport._
  import sysmo.reform.shared.table.Transport._
  import sysmo.reform.shared.types.Transport._
  import sysmo.reform.shared.query.Transport._

  def query(q: Query): F[Table[F]] = do_call[Query, LocalTable]("query", q)
    .map(local_table => new Table[Future] {
      override implicit val mt: MonadThrow[F] = MonadThrow[FRemote]
      override def schema: Schema = local_table.schema
      override def nrow: F[Int] = ???
      override def row_iter: MonadicIterator[F, Table.Row] = ???
      override def cache: F[LocalTable] = mt.pure(local_table)
    })
}

class RemoteConstructors(implicit ec: ExecutionContext) extends Constructors[FRemote] {
  implicit val mt: MonadThrow[FRemote] = MonadThrow[FRemote]
  override def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordInstance[F]] =
    FRemote(LocalObjects.record[FRemote](dtype, id, parent)(mt))
  override def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayInstance[F]] =
    FRemote(LocalObjects.array(dtype, id, parent)(mt))
}


class FutureRuntime(val type_system: TypeSystem)(implicit ec: ExecutionContext)
  extends RFRuntime[Future] {
  implicit val mt: MonadThrow[FRemote] = MonadThrow[FRemote]
  override val constructors: Constructors[F] = new RemoteConstructors
  val remote: RemoteEndpoints = new RemoteEndpoints

  override def list: MonadicIterator[F, ObjectProxy] = ???

  override def count: F[Int] = ???

  override def count(q: Query): F[Int] = ???

  override def run_query(q: Query): F[Table[F]] = remote.query(q)

  override def dispatch(action: RuntimeAction): F[Unit] = ???

//  override implicit val mt: MonadThrow[F] = _

  override def get(id: ObjectId): F[RTO] = ???

  override def create_record(dtype: RecordType, parent: Option[ObjectId]): F[RecordInstance[F]] = ???

  override def create_array(dtype: ArrayType, parent: Option[ObjectId]): F[ArrayInstance[F]] = ???

  override def update_record(id: ObjectId, field_values: Seq[(String, Value)]): F[RecordInstance[F]] = ???

  override def remove(id: ObjectId): F[Unit] = ???
}

//class FutureRuntimeQueryService(rutime: FutureRuntime) extends SQLQueryService[F] {
//  override def generate_sql(q: Query): F[SQLQuery] = ???
//
//  override def run_query(sql: SQLQuery): F[Table[F]] = ???
//
//  override implicit val mt: MonadThrow[F] = _
//
//  override def list_tables(): F[Seq[Schema]] = ???
//
//  override def table_schema(table_id: String): F[Schema] = ???
//}