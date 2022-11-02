package sysmo.reform.shared.data

import cats.MonadThrow
import sysmo.reform.shared.types.RecordFieldType

import java.util.Date

sealed trait Data

sealed trait LocalData extends Data

trait ValueFormatter



trait Value extends LocalData {
  def _get: Option[_]
  def get[U](implicit conv: ValueExtractor[U]): Option[U] =
    conv.extract(this)
  def is_set: Boolean
  def not_set: Boolean
  def is_na: Boolean = not_set
  override def equals(obj: Any): Boolean = obj match {
    case v_other: Value => _get == v_other._get
    case _ => false
  }

  def show(formatter: Option[ValueFormatter] = None): String
}

object Value extends ValueImpl

trait RecordFieldCodec[_F[+_]] {
  implicit val mt: MonadThrow[_F]
  type F[+X] = _F[X]
  type Record
  def read_value(field: RecordFieldType, record: Record): F[Value]
  def write_value(field: RecordFieldType, record: Record, value: Value): F[Unit]
}
