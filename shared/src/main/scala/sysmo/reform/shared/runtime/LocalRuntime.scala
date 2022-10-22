package sysmo.reform.shared.runtime
import sysmo.reform.shared.data.{ObjectId, ObjectIdSupplier, UUIDSupplier, Value}
import sysmo.reform.shared.types.{ArrayType, AtomicDataType, DataType, RecordFieldType, RecordType}
import sysmo.reform.shared.util.MonadicIterator

import scala.collection.mutable
import cats.MonadThrow
import sysmo.reform.shared.expr.{CommonPredicate, ContainmentPredicate, LogicalAnd, LogicalNot, LogicalOr, NumericalPredicate, StringPredicate, TypePredicateExpression}
import sysmo.reform.shared.sources.SourceAction
import sysmo.reform.shared.table.{BasicQuery, Query, QueryFilter}
import sysmo.reform.shared.util.containers.FLocal

class LocalRuntime() extends RFRuntime[FLocal] {
  override protected val objectid_supplier: ObjectIdSupplier = new UUIDSupplier()
  val mt: MonadThrow[FLocal] = MonadThrow[FLocal]
  private val objects: mutable.Map[ObjectId, RTO] = mutable.HashMap()
  private val dtype_index: mutable.Map[DataType, mutable.HashSet[ObjectId]] = mutable.HashMap()
  val constructors: Constructors[F] = LocalRuntime.constructors
  override def get(id: ObjectId): F[RTO] = objects.get(id) match {
    case Some(x) => mt.pure(x)
    case None => mt.raiseError(new NoSuchElementException(s"$id"))
  }
  override def put[T <: RTO](id: ObjectId, obj: T): F[T] = {
    objects.put(id, obj)
    obj.dtype match {
      case _: AtomicDataType =>
      case _ => dtype_index.getOrElse(obj.dtype, mutable.HashSet()).add(id)
    }

    mt.pure(obj)
  }
  override def remove(id: ObjectId): F[Unit] = {
    objects.get(id) match {
      case Some(inst) => inst.dtype match {
        case _: AtomicDataType =>
        case _ => dtype_index.get(inst.dtype).foreach(ids => ids.remove(id))
      }
      case None =>
    }
    objects.remove(id)
    mt.pure()
  }

  override def list: MonadicIterator[F, ObjectProxy] =
    MonadicIterator.from_iterator(
      objects.values.map(x => ObjectProxy(x.id, x.dtype, x.parent, Value.empty)).iterator
    )

  def count: F[Int] = FLocal(objects.size)

  // TODO Implement query !!!!!!!!!!!!!!!!!!!!!!!!!!
  override def count(q: Query): F[Int] = FLocal(objects.size)
  override def run_query(q: Query): MonadicIterator[F, RFObject[F]] = {
    throw new NotImplementedError
//    q match {
//      case BasicQuery(source, columns, filter, sort, range) => filter match {
//        case Some(QueryFilter(expr)) => expr match {
//          case expression: TypePredicateExpression => ???
//          case _ => throw new NotImplementedError(s"Cannot handle filter expression $expr")
//        }
//        case None => MonadicIterator.from_iterator(objects.values.iterator)
//      }
//    }

  }

  override def dispatch(action: RuntimeAction): F[Unit] = {
    logger.info(action.toString)
    action match {
      case SetValue(id, value) => {
        for {
          obj <- get(id)
          _ <- obj match {
            case atomic_object: AtomicObject[F] => atomic_object.update_value(value)
            case recordObject: RecordObject[F] => ???
            case arrayObject: ArrayObject[F] => ???
          }
        } yield ()
      }
      case _ => ???
    }
    mt.pure()
  }
}

object LocalRuntime {
  trait LocalMT {
    val mt: MonadThrow[FLocal] = MonadThrow[FLocal]
  }
  case class AtomicObjectImpl(dtype: AtomicDataType, id: ObjectId, var value: Value, parent: Option[ObjectId])
    extends AtomicObject[FLocal] with LocalMT {
    override def update_value(v: Value): F[Unit] = {
      value = v
      mt.pure()
    }
  }

  case class RecordObjectImpl(dtype: RecordType, id: ObjectId, parent: Option[ObjectId])
    extends RecordObject[FLocal] with LocalMT {
    protected val children: mutable.HashMap[String, ObjectId] = new mutable.HashMap()
    override def own_children: MIter = {
      MonadicIterator.from_iterator[F, (String, ObjectId)](children.iterator)
        .flat_map(c => runtime.get(c._2))
    }
    override def fields: MonadicIterator[F, RecordFieldInstance] =
      MonadicIterator.from_iterator[F, RecordFieldType](dtype.fields.iterator)
        .map(ftype => RecordFieldInstance(ftype, children(ftype.name)))

    override private[runtime] def set_field(name: String, instance: ObjectId): F[Unit] =
      dtype.field_index(name) match {
        case Some(i) => {
          children(name) = instance
          mt.pure()
        }
        case None => mt.raiseError(new NoSuchFieldException(s"$name in Record ${dtype.symbol}"))
      }
  }

  case class ArrayObjectImpl(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId])
    extends ArrayObject[FLocal] with LocalMT {
    protected val children: mutable.ArrayBuffer[ObjectId] = new mutable.ArrayBuffer()
    override def own_children: MIter = {
      MonadicIterator.from_iterator[F, ObjectId](children.iterator)
        .flat_map(id => runtime.get(id))
    }

    def elements: MonadicIterator[F, ArrayElementInstance[F]] =
      MonadicIterator.from_iterator[F, ArrayElementInstance[F]](
        children.zipWithIndex.map(x => ArrayElementInstance[F](x._2, x._1)).iterator
      )

    private[runtime] def add_element(instance: ObjectId): F[Unit] =
      mt.pure(children.append(instance))
  }

  object constructors extends Constructors[FLocal] {
    override def atomic(dtype: AtomicDataType, id: ObjectId, value: Value, parent: Option[ObjectId]): F[AtomicObject[F]] =
      FLocal(AtomicObjectImpl(dtype, id, value, parent))
    override def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordObject[F]] =
      FLocal(RecordObjectImpl(dtype, id, parent))
    override def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayObject[F]] =
      FLocal(ArrayObjectImpl(dtype, id, parent))
  }

  def apply(): LocalRuntime = new LocalRuntime()

}
