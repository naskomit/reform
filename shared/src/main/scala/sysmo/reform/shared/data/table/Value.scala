package sysmo.reform.shared.data.table

import sysmo.reform.shared.data.table.{VectorType => VT}

case class Value(v: Option[_], tpe: VectorType.Value) {
  def is_set: Boolean = v.isDefined
  def not_set: Boolean = v.isEmpty
  def is_na: Boolean = v.isEmpty



  def as_real: Option[Double] = v match {
    case Some(v: Double) => Some(v)
    case Some(v: Int) => Some(v.toDouble)
    case _ => None
  }

  def as_int: Option[Int] = v match {
    case Some(v: Double) => Some(v.round.toInt)
    case Some(v: Int) => Some(v)
    case _ => None
  }

  def as_bool: Option[Boolean] = v match {
//    case v: Double => if (v == 0.0) Some(false) else Some(true)
    case Some(v: Int) => if (v == 0) Some(false) else Some(true)
    case Some(v: Boolean) => Some(v)
    case _ => None
  }

  def as_char: Option[String] = v match {
    case Some(v: Double) => Some(v.toString)
    case Some(v: Int) => Some(v.toString)
    case Some(v: Boolean) => Some(v.toString)
    case Some(v: String) => Some(v)
    case None => None
  }

  def as_date: Option[java.util.Date] = v match {
    case Some(v: Double) => Some(new java.util.Date(v.toLong))
    case _ => None
  }
}

object Value {
  def apply(v: Option[_], tpe: VT.Value): Value = new Value(v, tpe)
  object Empty extends Value(None, VectorType.Int)
}

