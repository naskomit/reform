package sysmo.reform.shared.field

import scala.reflect.ClassTag
import scala.runtime.Nothing$

sealed trait Value[+T] {
  protected val v: Option[T]
  def descr: Option[String] = None
  def make_descr: String = descr.getOrElse(v match {
    case Some(x) => x.toString
    case None => "None"
  })
  def get: T = v.get
  def get_opt: Option[T] = v

  def is_set: Boolean = v.isDefined
  def not_set: Boolean = v.isEmpty
  def is_na: Boolean = v.isEmpty

  def as_real: Option[Double] = None
  def as_int: Option[Int] = None
  def as_bool: Option[Boolean] = None
  def as_char: Option[String] = v.map(_.toString)
  def as_date: Option[java.util.Date] = None
  def as_record[U : ClassTag]: Option[U] = None
  def equals(other: Value[_]): Boolean = {
    (v, other.v) match {
      case (Some(_v), Some(other_v)) => _v == other_v
      case (None, None) => true
      case _ => false
    }
  }
}

object Value {
  case object NoValue extends Value[Nothing] {
    val v: Option[Nothing] = None
  }

  case object AllValues extends Value[Any] {
    val v: Option[Nothing] = None
    override def descr: Option[String] = Some("ALL")
  }

  case class RealValue(v: Option[Double]) extends Value[Double] {
    override def as_real: Option[Double] = v
    override def as_int: Option[Int] = v.map(_.round.toInt)
  }

  case class IntValue(v: Option[Int]) extends Value[Int] {
    override def as_real: Option[Double] = v.map(_.toDouble)
    override def as_int: Option[Int] = v
    override def as_bool: Option[Boolean] = v.map(_ != 0)
  }

  case class BoolValue(v: Option[Boolean]) extends Value[Boolean] {
    override def as_int: Option[Int] = v.map(if (_) 1 else 0)
    override def as_bool: Option[Boolean] = v

  }

  case class CharValue(v: Option[String]) extends Value[String] {
    override def as_char: Option[String] = v
  }

  case class DateValue(v: Option[Double]) extends Value[Double] {
    override def as_date: Option[java.util.Date] = v.map(x => new java.util.Date(x.toLong))
  }

  case class CategoricalValue(v: Option[Int], categories: Seq[String]) extends Value[Int] {
    override def as_int: Option[Int] = v
    override def as_char: Option[String] = v match {
      case Some(x: Int) => Some(categories(x))
      case None => None
    }
  }

  case class RecordValue[T <: Record](v: Option[T]) extends Value[T] {
    override def as_record[U : ClassTag]: Option[U] = v match {
      case Some(x: U) => Some(x)
      case Some(x) => None
      case None => None
    }
  }

  def real(x: Option[Double]): RealValue = RealValue(x)
  def real(x: Double): RealValue = RealValue(Some(x))
  def int(x: Option[Int]): IntValue = IntValue(x)
  def int(x: Int): IntValue = IntValue(Some(x))
  def bool(x: Option[Boolean]): BoolValue = BoolValue(x)
  def bool(x: Boolean): BoolValue = BoolValue(Some(x))
  def char(x: Option[String]): CharValue = CharValue(x)
  def char(x: String): CharValue = CharValue(Some(x))
  def date(x: Option[Double]): DateValue = DateValue(x)
  def date(x: Double): DateValue = DateValue(Some(x))
  def date(x: java.util.Date): DateValue = DateValue(Some(x.getTime.toDouble))
  def record[T <: Record](x: T): RecordValue[T] = RecordValue(Some(x))
  def record[T <: Record](x: Option[T]): RecordValue[T] = RecordValue(x)
  def empty(tpe: FieldType = FieldType.Int): Value[_] = tpe match {
    case FieldType.Int => int(None)
    case FieldType.Real => real(None)
    case FieldType.Bool => bool(None)
    case FieldType.Char => char(None)
  }
  def empty[T <: Record]: Value[_] = record[T](None)
//  def empty: Value[_] = IntValue(None)
}

