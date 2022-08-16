package sysmo.reform.shared.runtime

import cats.MonadThrow
import sysmo.reform.shared.data.{ObjectId, ObjectIdSupplier, UUIDSupplier}

import scala.reflect.ClassTag
//import sysmo.reform.shared.util.MonadicIterator

trait ObjectRuntime[_F[+_]] {
  type F[+X] = _F[X]
  val mt: MonadThrow[F]
  protected val objectid_supplier: ObjectIdSupplier
  type RTO = RuntimeObject[F]

  def get(id: ObjectId): F[RTO]
  def put[T <: RTO](id: ObjectId, obj: T): F[T]
  def remove(id: ObjectId): F[Unit]

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
    mt.flatMap(
      mt.flatMap(get(id))(obj => obj.own_children.fold_left(())((acc, child_id) => acc))
    )(_ => remove(id))
  }

  def create_object[T <: RTO](create_fn: ObjectId => T): T = {
    val obj = create_fn(objectid_supplier.new_id)
    obj.runtime = this
    put(obj.id, obj)
    obj
  }

}
