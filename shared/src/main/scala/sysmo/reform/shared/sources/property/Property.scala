package sysmo.reform.shared.sources.property

import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.runtime.RecordFieldInstance
import sysmo.reform.shared.types.DataType

sealed trait Property {
  def name: String
  def descr: String
  def dtype: DataType
  def value: Value
}

case class FieldProperty(field: RecordFieldInstance) extends Property {
  override def name: String = field.ftype.name
  override def descr: String = field.ftype.make_descr
  override def dtype: DataType = field.ftype.dtype
  override def value: Value = field.value
}

object Property {
  sealed trait SizeHint
  object SizeHint {
    case object ExtraShort extends SizeHint
    case object Short extends SizeHint
    case object Medium extends SizeHint
    case object Long extends SizeHint
    case object ExtraLong extends SizeHint
    case object FullWidth extends SizeHint
  }

}