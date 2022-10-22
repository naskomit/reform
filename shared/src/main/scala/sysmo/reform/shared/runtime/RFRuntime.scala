package sysmo.reform.shared.runtime

import cats.MonadThrow
import cats.implicits._
import sysmo.reform.shared.types.{ArrayType, AtomicDataType, RecordFieldType, RecordType}
import sysmo.reform.shared.data.{ObjectId, ObjectIdSupplier, Value}
import sysmo.reform.shared.logging.Logging
import sysmo.reform.shared.table.Table.Schema
import sysmo.reform.shared.table.{LocalRowBasedTable, LocalTable, Query, Table, TableService}
import sysmo.reform.shared.util.MonadicIterator

import scala.reflect.ClassTag

trait Constructors[_F[+_]] {
  type F[+X] = _F[X]
  def atomic(dtype: AtomicDataType, id: ObjectId, value: Value, parent: Option[ObjectId]): F[AtomicObject[F]]
  def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordObject[F]]
  def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayObject[F]]

}

trait RFRuntime[_F[+_]] extends Logging {
  type F[+X] = _F[X]
  implicit val mt: MonadThrow[F]
  protected val objectid_supplier: ObjectIdSupplier
  type RTO = RFObject[F]

  def get(id: ObjectId): F[RTO]
  def put[T <: RTO](id: ObjectId, obj: T): F[T]
  def remove(id: ObjectId): F[Unit]
  def list: MonadicIterator[F, ObjectProxy]
  def count: F[Int]
  def count(q: Query): F[Int]
  def run_query(q: Query): MonadicIterator[F, RFObject[F]]
  def dispatch(action: RuntimeAction): F[Unit]
  val constructors: Constructors[F]

  def get_typed[T <: RTO](id: ObjectId)(implicit tag: ClassTag[T]): F[T] =
    mt.flatMap(get(id)) {
      case x: T => mt.pure(x)
      case x => mt.raiseError(new IllegalArgumentException(s"Incorrect object type, expected ${tag.getClass.getName}, found ${x.getClass.getName}"))
    }

  def update[T <: RTO](id: ObjectId, f: T => T)(implicit tag: ClassTag[T]): F[T] = {
    mt.flatMap(get_typed[T](id)) {obj =>
      val new_obj = f(obj)
      new_obj.runtime = this
      put(id, new_obj)
    }
  }

  def remove_recursive(id: ObjectId): F[Unit] = {
    for {
      obj <- get(id)
      _ <- obj.own_children.map(child => remove_recursive(child.id)).traverse()
    } yield ()
  }

  def create_object[T <: RTO](create_fn: ObjectId => F[T]): F[T] = {
    val rt = this
    for {
      obj <- create_fn(objectid_supplier.new_id)
      res <- {
        obj.runtime = rt
        put(obj.id, obj)
      }
    } yield res
  }


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
        f_id("parent") + f_char("value")
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
            .flat_map{pr =>
              mt.map(
                pr.dtype match {
                  case _: AtomicDataType => mt.map(runtime.get(pr.id))(inst =>  inst.asInstanceOf[AtomicObject[F]].value)
                  case _ => mt.pure(Value.empty)
                }
              )(value => pr.copy(value = value))
            }
            .map(pr =>
              new Table.Row {
                override def schema: Schema = sch
                override protected def _get(col: Int): Value = col match {
                  case 0 => pr.id
                  case 1 => pr.dtype.show
                  case 2 => pr.parent.map(Value(_)).getOrElse(Value.empty)
                  case 3 => pr.value
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