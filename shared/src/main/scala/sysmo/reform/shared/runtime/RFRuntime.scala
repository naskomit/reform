package sysmo.reform.shared.runtime

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.types.{ArrayType, DataType, RecordFieldType, RecordType, TypeSystem}
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.logging.Logging
import sysmo.reform.shared.query.Query
import sysmo.reform.shared.table.Table.Schema
import sysmo.reform.shared.table.{LocalTable, Table, TableService}
import sysmo.reform.shared.util.MonadicIterator

import scala.reflect.ClassTag

trait Constructors[_F[+_]] {
  type F[+X] = _F[X]
  def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordInstance[F]]
  def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayInstance[F]]

}

trait ObjectStorage[_F[+_]] extends Logging {
  type F[+X] = _F[X]
  implicit val mt: MonadThrow[F]
  type RTO = RFObject[F]

  def get(id: ObjectId): F[RTO]
  def get_typed[T <: RTO](id: ObjectId)(implicit tag: ClassTag[T]): F[T] =
    mt.flatMap(get(id)) {
      case x: T => mt.pure(x)
      case x => mt.raiseError(new IllegalArgumentException(s"Incorrect object type, expected ${tag.getClass.getName}, found ${x.getClass.getName}"))
  }

  def create_record(dtype: RecordType, parent: Option[ObjectId]): F[RecordInstance[F]]
  def create_array(dtype: ArrayType, parent: Option[ObjectId]): F[ArrayInstance[F]]

  def update_record(id: ObjectId, field_values: Seq[(String, Value)]): F[RecordInstance[F]]
  def update_record(id: ObjectId, f: RecordInstance[F] => RecordInstance[F]): F[RecordInstance[F]] = {
    mt.flatMap(get_typed[RecordInstance[F]](id)) {obj =>
      val new_obj = f(obj)
      new_obj.runtime = obj.runtime
      new_obj.fields.map(f => (f.ftype.name, f.value)).traverse().flatMap(
        fv => update_record(id, fv)
      )
    }
  }
  def remove(id: ObjectId): F[Unit]

  def remove_recursive(id: ObjectId): F[Unit] = {
    for {
      obj <- get(id)
      _ <- obj.own_children.map(remove_recursive).traverse()
    } yield ()
  }

}

trait RFRuntime[_F[+_]] extends ObjectStorage[_F] {
  val constructors: Constructors[F]
  val type_system: TypeSystem
  def list: MonadicIterator[F, ObjectProxy]
  def count: F[Int]
  def count(q: Query): F[Int]
  def run_query(q: Query): F[Table[F]]
  def dispatch(action: RuntimeAction): F[Unit]
}

object RFRuntime {
  import Value.implicits._

  class TableView[_F[+_]](runtime: RFRuntime[_F]) extends TableService[_F] {
    override val mt: MonadThrow[F] = runtime.mt
    override def list_tables(): F[Seq[RecordType]] = mt.map(table_schema(""))(x => Seq(x))

    override def table_schema(table_id: String): F[RecordType] = {
      import RecordFieldType.constr._
      val schema = RecordType("Runtime") +
        f_id("id") + f_char("data_type") +
        f_id("parent")
      mt.pure(schema)
    }

    override def query_table(q: Query): F[Table[F]] = {
      val object_list_it = runtime.list
      mt.map(table_schema(""))(sch =>
        new Table[F] {
          override val mt: MonadThrow[F] = runtime.mt
          override def schema: RecordType = sch
          override def nrow: F[Int] = runtime.count

          override def row_iter: MonadicIterator[F, Table.Row] = object_list_it
            .map(pr =>
              new Table.Row {
                override def schema: Schema = sch
                override protected def _get(col: Int): Value = col match {
                  case 0 => Value(pr.id)
                  case 1 => Value(pr.dtype.show)
                  case 2 => pr.parent.map(p => Value(p)).getOrElse(Value.empty)
                }
              }
          )
        }
      )
    }
  }

  object implicits {

    implicit class runtime2table[F[+_]](runtime: RFRuntime[F]) extends TableView[F](runtime)
  }
}

trait RuntimeConstructor[_F[+_]] {
  def apply(ts: TypeSystem): RFRuntime[_F]
}