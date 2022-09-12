package sysmo.reform.shared.sources.property

import sysmo.reform.shared.data.Value
import sysmo.reform.shared.types.DataType

trait Property {
  type Id
  def id: Id
  def name: String
  def descr: String
  def dtype: DataType
  def value: Value
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