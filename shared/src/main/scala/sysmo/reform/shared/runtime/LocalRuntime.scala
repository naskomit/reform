package sysmo.reform.shared.runtime
import sysmo.reform.shared.data.{ObjectId, ObjectIdSupplier, UUIDSupplier, Value}
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, DataType, PrimitiveDataType, RecordFieldType, RecordType}
import sysmo.reform.shared.util.MonadicIterator

import scala.collection.mutable
import cats.MonadThrow
import sysmo.reform.shared.expr.{CommonPredicate, ContainmentPredicate, LogicalAnd, LogicalNot, LogicalOr, NumericalPredicate, StringPredicate, TypePredicateExpression}
import sysmo.reform.shared.sources.SourceAction
import sysmo.reform.shared.table.{BasicQuery, Query, QueryFilter}
import sysmo.reform.shared.util.containers.FLocal

import scala.collection.mutable.ArrayBuffer

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
      case _: PrimitiveDataType =>
      case _ => dtype_index.getOrElse(obj.dtype, mutable.HashSet()).add(id)
    }

    mt.pure(obj)
  }
  override def remove(id: ObjectId): F[Unit] = {
    objects.get(id) match {
      case Some(inst) => inst.dtype match {
        case _: PrimitiveDataType =>
        case _ => dtype_index.get(inst.dtype).foreach(ids => ids.remove(id))
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

object LocalRuntime {
  trait LocalMT {
    val mt: MonadThrow[FLocal] = MonadThrow[FLocal]
  }
  case class RecordObjectImpl(dtype: RecordType, id: ObjectId, parent: Option[ObjectId])
    extends RecordInstance[FLocal] with LocalMT {
    import Value.implicits._
    protected val children: ArrayBuffer[Value] = ArrayBuffer.fill(dtype.fields.size)(Value.empty)
    println(s"Allocated ${dtype.fields.size} children for ${dtype.symbol}")
    override def own_children: MIter = {
      val child_fields = children.iterator.zipWithIndex.collect {
        case (value, i) if dtype.fields(i).isInstanceOf[CompoundDataType] =>
          value.get[ObjectId].get
      }
      MonadicIterator.from_iterator[F, ObjectId](child_fields)
    }
    override def fields: MonadicIterator[F, RecordFieldInstance] =
      MonadicIterator.from_iterator[F, (RecordFieldType, Value)](dtype.fields.iterator.zip(children))
        .map{case(ftype, child) => RecordFieldInstance(ftype, child)}

    override private[runtime] def set_field(name: String, value: Value): F[Unit] =
      dtype.field_index(name) match {
        case Some(i) => {
          children(i) = value
          mt.pure()
        }
        case None => mt.raiseError(new NoSuchFieldException(s"$name in Record ${dtype.symbol}"))
      }
  }

  case class ArrayObjectImpl(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId])
    extends ArrayInstance[FLocal] with LocalMT {
    protected val children: mutable.ArrayBuffer[ObjectId] = new mutable.ArrayBuffer()
    override def own_children: MIter = {
      MonadicIterator.from_iterator[F, ObjectId](children.iterator)
    }

    def elements: MonadicIterator[F, ArrayElementInstance[F]] =
      MonadicIterator.from_iterator[F, ArrayElementInstance[F]](
        children.zipWithIndex.map(x => ArrayElementInstance[F](x._2, x._1)).iterator
      )

    private[runtime] def add_element(instance: ObjectId): F[Unit] =
      mt.pure(children.append(instance))
  }

  object constructors extends Constructors[FLocal] {
    override def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordInstance[F]] =
      FLocal(RecordObjectImpl(dtype, id, parent))
    override def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayInstance[F]] =
      FLocal(ArrayObjectImpl(dtype, id, parent))
  }

  def apply(): LocalRuntime = new LocalRuntime()

}
