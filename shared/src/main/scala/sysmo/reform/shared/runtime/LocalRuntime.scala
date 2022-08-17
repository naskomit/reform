package sysmo.reform.shared.runtime
import sysmo.reform.shared.data.{ObjectId, ObjectIdSupplier, UUIDSupplier, Value}
import sysmo.reform.shared.types.{ArrayType, AtomicDataType, RecordType}
import sysmo.reform.shared.util.MonadicIterator

import scala.collection.mutable
import cats.MonadThrow
import sysmo.reform.shared.runtime.LocalRuntime.constructors

class LocalRuntime() extends ObjectRuntime[LocalRuntime.Result] {
  override protected val objectid_supplier: ObjectIdSupplier = new UUIDSupplier()
  val mt: MonadThrow[LocalRuntime.Result] = MonadThrow[LocalRuntime.Result]
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
  type Result[+T] = Either[Throwable, T]

  trait LocalMT {
    val mt: MonadThrow[Result] = MonadThrow[Result]
  }
  case class AtomicObjectImpl(val dtype: AtomicDataType, val id: ObjectId, val value: Value, val parent: Option[ObjectId])
    extends AtomicObject[Result] with LocalMT

  case class RecordObjectImpl(val dtype: RecordType, val id: ObjectId, val parent: Option[ObjectId])
    extends RecordObject[Result] with LocalMT {
    protected val children: mutable.ArrayBuffer[ObjectId] = new mutable.ArrayBuffer()
    override def own_children: MIter = {
      MonadicIterator.from_iterator[F, ObjectId](children.iterator)
        .flat_map(id => runtime.get(id))
    }
    override def fields: MonadicIterator[F, RecordFieldInstance[F]] = ???
  }

  case class ArrayObjectImpl(val dtype: ArrayType, val id: ObjectId, val parent: Option[ObjectId])
    extends ArrayObject[Result] with LocalMT {
    protected val children: mutable.ArrayBuffer[ObjectId] = new mutable.ArrayBuffer()
    override def own_children: MIter = {
      MonadicIterator.from_iterator[F, ObjectId](children.iterator)
        .flat_map(id => runtime.get(id))
    }

    def elements: MonadicIterator[F, ArrayElementInstance[F]] = ???
  }

  object constructors extends Constructors[Result] {
    override def atomic(dtype: AtomicDataType, id: ObjectId, value: Value, parent: Option[ObjectId]): F[AtomicObject[F]] =
      Right(AtomicObjectImpl(dtype, id, value, parent))
    override def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordObject[F]] =
      Right(RecordObjectImpl(dtype, id, parent))
    override def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayObject[F]] =
      Right(ArrayObjectImpl(dtype, id, parent))
  }

  def apply(): LocalRuntime = new LocalRuntime()

}
