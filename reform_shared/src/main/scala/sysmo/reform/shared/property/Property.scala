package sysmo.reform.shared.property

import sysmo.reform.shared.field.{FieldType, Value}

import scala.reflect.ClassTag


trait Property[K, V] {
  def id: K
  def name: String
  def descr: String
  def get_type: FieldType
  def get_value: Value[V]
  def update_value(v: Value[V]): Either[Throwable, Unit]
  def size_hint: Property.SizeHint = Property.Medium
}

object Property {
  sealed trait SizeHint
  case object ExtraShort extends SizeHint
  case object Short extends SizeHint
  case object Medium extends SizeHint
  case object Long extends SizeHint
  case object ExtraLong extends SizeHint
  case object FullWidth extends SizeHint
}
