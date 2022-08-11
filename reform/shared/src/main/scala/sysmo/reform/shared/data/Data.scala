package sysmo.reform.shared.data

import java.util.Date

sealed trait Data

sealed trait LocalData extends Data {

}

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
}

object Value extends ValueImpl

