package sysmo.reform.shared.runtime
import sysmo.reform.shared.data.{ObjectId, ObjectIdSupplier, UUIDSupplier, Value}
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, DataType, MultiReferenceType, PrimitiveDataType, RecordFieldType, RecordType, ReferenceType, TypeSystem}
import sysmo.reform.shared.util.MonadicIterator

import scala.collection.mutable
import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.query.{BasicQuery, Query, QueryFilter}
import sysmo.reform.shared.table.Table
import sysmo.reform.shared.util.containers.FLocal

class CategorialIndex[K, V] {
  private val index_map: mutable.Map[K, mutable.HashSet[V]] = mutable.HashMap()
  def add_entry(key: K, value: V): Boolean = {
    index_map.getOrElseUpdate(key, mutable.HashSet()).add(value)
  }

  def remove_entry(key: K, value: V): Boolean = {
    index_map.getOrElseUpdate(key, mutable.HashSet()).remove(value)
  }
}

object CategoricalIndex {
  def apply[K, V](): CategorialIndex[K, V] = new CategorialIndex[K, V]
}


class LocalRuntime(val type_system: TypeSystem) extends RFRuntime[FLocal] {
  protected val objectid_supplier: ObjectIdSupplier = new UUIDSupplier()
  val mt: MonadThrow[FLocal] = MonadThrow[FLocal]
  private val objects: mutable.Map[ObjectId, RTO] = mutable.HashMap()
  private val dtype_index: CategorialIndex[DataType, ObjectId] = CategoricalIndex()
  val constructors: Constructors[F] = LocalRuntimeConstructor.constructors

  override def get(id: ObjectId): F[RTO] = objects.get(id) match {
    case Some(x) => mt.pure(x)
    case None => mt.raiseError(new NoSuchElementException(s"$id"))
  }

  def create_record(dtype: RecordType, parent: Option[ObjectId]): F[RecordInstance[F]] = {
    val oid = objectid_supplier.new_id
    for {
      instance <- constructors.record(dtype, oid, parent)
        .map(inst => {
          inst.runtime = this;
          objects.addOne(oid, inst)
          dtype_index.add_entry(dtype, oid)
          inst
        })
    } yield instance
  }

  def create_array(dtype: ArrayType, parent: Option[ObjectId]): F[ArrayInstance[F]] = {
    val oid = objectid_supplier.new_id
    for {
          instance <- constructors.array(dtype, oid, parent)
            .map(inst => {
              inst.runtime = this; inst
              objects.addOne(oid, inst)
              dtype_index.add_entry(dtype, oid)
              inst
            })
    } yield instance
  }


  override def update_record(id: ObjectId, field_values: Seq[(String, Value)]): F[RecordInstance[F]] = {
    objects.get(id) match {
      case Some(rec: RecordInstance[F]) => (field_values.map {
        case (field, value) => rec.set_field(field, value)
      }).traverse(identity).map(_ => rec)
      case None => mt.raiseError(new IllegalArgumentException(s"Failed updating record ${id}"))
    }
  }

  override def remove(id: ObjectId): F[Unit] = {
    objects.get(id) match {
      case Some(inst) => inst.dtype match {
        case _: PrimitiveDataType =>
        case _ => dtype_index.remove_entry(inst.dtype, id)
      }
      case None =>
    }
    objects.remove(id)
    mt.pure()
  }

  override def list: MonadicIterator[F, ObjectProxy] =
    MonadicIterator.from_iterator(
      objects.values.map(x => ObjectProxy(x.id, x.dtype, x.parent)).iterator
    )

  def count: F[Int] = FLocal(objects.size)

  // TODO Implement query !!!!!!!!!!!!!!!!!!!!!!!!!!
  override def count(q: Query): F[Int] = FLocal(objects.size)

//  {
//    throw new NotImplementedError
////    q match {
////      case BasicQuery(source, columns, filter, sort, range) => filter match {
////        case Some(QueryFilter(expr)) => expr match {
////          case expression: TypePredicateExpression => ???
////          case _ => throw new NotImplementedError(s"Cannot handle filter expression $expr")
////        }
////        case None => MonadicIterator.from_iterator(objects.values.iterator)
////      }
////    }
//
//  }

  override def run_query(q: Query): F[Table[F]] =
    mt.raiseError(new NotImplementedError("LocalRuntime.run_table_query()"))

  override def dispatch(action: RuntimeAction): F[Unit] = {
    logger.info(action.toString)
    action match {
      case SetFieldValue(id, new_value) => {
        for {
          obj <- get(id)
          _ <- obj match {
            case x: RecordInstance[F] => x.set_field(new_value.ftype.name, new_value.value)
            case x: ArrayInstance[F] => ???
          }
        } yield ()
      }
      case _ => ???
    }
    mt.pure()
  }
}

object LocalRuntimeConstructor extends RuntimeConstructor[FLocal] {

  object constructors extends Constructors[FLocal] {
    override def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordInstance[F]] =
      FLocal(LocalObjects.record(dtype, id, parent))
    override def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayInstance[F]] =
      FLocal(LocalObjects.array(dtype, id, parent))
  }

  def apply(ts: TypeSystem): RFRuntime[FLocal] = new LocalRuntime(ts)

}
