package sysmo.reform.shared.runtime
import sysmo.reform.shared.data.{ObjectId, ObjectIdSupplier, UUIDSupplier, Value}
import sysmo.reform.shared.types.{ArrayType, AtomicDataType, RecordFieldType, RecordType}
import sysmo.reform.shared.util.MonadicIterator

import scala.collection.mutable
import cats.MonadThrow
import sysmo.reform.shared.runtime.LocalRuntime.constructors

class LocalRuntime() extends ObjectRuntime[FLocal] {
  override protected val objectid_supplier: ObjectIdSupplier = new UUIDSupplier()
  val mt: MonadThrow[FLocal] = MonadThrow[FLocal]
  val objects: mutable.Map[ObjectId, RTO] = mutable.HashMap()
  val constructors: Constructors[F] = LocalRuntime.constructors
  override def get(id: ObjectId): F[RTO] = objects.get(id) match {
    case Some(x) => mt.pure(x)
    case None => mt.raiseError(new NoSuchElementException(s"$id"))
  }
  override def put[T <: RTO](id: ObjectId, obj: T): F[T] = {
    objects.put(id, obj)
    mt.pure(obj)
  }
  override def remove(id: ObjectId): F[Unit] = {
    objects.remove(id)
    mt.pure()
  }


}

object LocalRuntime {
  trait LocalMT {
    val mt: MonadThrow[FLocal] = MonadThrow[FLocal]
  }
  case class AtomicObjectImpl(val dtype: AtomicDataType, val id: ObjectId, val value: Value, val parent: Option[ObjectId])
    extends AtomicObject[FLocal] with LocalMT

  case class RecordObjectImpl(val dtype: RecordType, val id: ObjectId, val parent: Option[ObjectId])
    extends RecordObject[FLocal] with LocalMT {
    protected val children: mutable.ArrayBuffer[ObjectId] = new mutable.ArrayBuffer()
    override def own_children: MIter = {
      MonadicIterator.from_iterator[F, ObjectId](children.iterator)
        .flat_map(id => runtime.get(id))
    }
    override def fields: MonadicIterator[F, RecordFieldInstance[F]] =
      MonadicIterator.from_iterator[F, (RecordFieldType, ObjectId)](dtype.fields.zip(children).iterator)
        .map {case (dtype, id) => RecordFieldInstance(dtype, id)}

    override private[runtime] def set_field(name: String, instance: ObjectId): F[Unit] =
      dtype.field_index(name) match {
        case Some(i) => {children(i) = instance; mt.pure()}
        case None => {mt.raiseError(new NoSuchFieldException(s"$name in Record ${dtype.symbol}"))}
      }
  }

  case class ArrayObjectImpl(val dtype: ArrayType, val id: ObjectId, val parent: Option[ObjectId])
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
      Right(AtomicObjectImpl(dtype, id, value, parent))
    override def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordObject[F]] =
      Right(RecordObjectImpl(dtype, id, parent))
    override def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayObject[F]] =
      Right(ArrayObjectImpl(dtype, id, parent))
  }

  def apply(): LocalRuntime = new LocalRuntime()

}
