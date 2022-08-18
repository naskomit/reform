package sysmo.reform.shared.runtime

import cats.MonadThrow
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.util.MonadicIterator
import sysmo.reform.shared.{types => TPE}


sealed trait RuntimeObject[_F[+_]] {
  type F[+X] = _F[X]
  type MIter = MonadicIterator[F, RuntimeObject[F]]
  val mt: MonadThrow[_F]
  type DType <: TPE.DataType
  def dtype: DType
  val id: ObjectId
  protected[runtime] var runtime: ObjectRuntime[F] = null
  def parent: Option[ObjectId]
  def own_children: MIter
  def get_runtime: ObjectRuntime[F] = runtime
}

trait AtomicObject[_F[+_]] extends RuntimeObject[_F] {
  override type DType = TPE.AtomicDataType
  def value: Value
  override def own_children: MIter =
    MonadicIterator.empty[F, RuntimeObject[F]](mt)
}

case class RecordFieldInstance[_F[+_]](dtype: TPE.RecordFieldType, instance: ObjectId)

trait RecordObject[_F[+_]] extends RuntimeObject[_F] {
  override type DType = TPE.RecordType
  private[runtime] def set_field(name: String, instance: ObjectId): F[Unit]
  def fields: MonadicIterator[F, RecordFieldInstance[F]]
}

case class ArrayElementInstance[_F[+_]](index: Int, instance: ObjectId)

trait ArrayObject[_F[+_]] extends RuntimeObject[_F] {
  override type DType = TPE.ArrayType
  def elements: MonadicIterator[F, ArrayElementInstance[F]]
  private[runtime] def add_element(instance: ObjectId): F[Unit]
}


