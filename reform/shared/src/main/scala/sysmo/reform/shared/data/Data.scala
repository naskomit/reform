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

case object NoValue extends Value {
  override def is_set: Boolean = false
  override def not_set: Boolean = true
  override def _get: Option[Nothing] = None
}

trait ValueExtractor[T] {
  def extract(in: Value): Option[T]
}

trait ValueConstructor[T] {
  def construct(in: T): Value
  def construct_opt(in: Option[T]): Value = in match {
    case Some(x) => construct(x)
    case None => NoValue
  }
}

object Value {
  trait ValueFromOption extends Value {
    val v: Option[_]
    override def is_set: Boolean = v.isDefined
    override def not_set: Boolean = v.isEmpty
  }


  case class RealValue(v: Option[Double]) extends ValueFromOption {
    override def _get: Option[Double] = v
  }

  case class IntValue(v: Option[Int]) extends ValueFromOption {
    override def _get: Option[Int] = v
  }

  case class LongValue(v: Option[Long]) extends ValueFromOption {
    override def _get: Option[Long] = v
  }

  case class CharValue(v: Option[String]) extends ValueFromOption {
    override def _get: Option[String] = v
  }

  case class BoolValue(v: Option[Boolean]) extends ValueFromOption {
    override def _get: Option[Boolean] = v
  }

  case class DateValue(v: Option[Date]) extends ValueFromOption {
    override def _get: Option[Date] = v
  }

  case class IdValue(v: Option[ObjectId]) extends ValueFromOption {
    override def _get: Option[ObjectId] = v
  }

  def apply[T, V <: Value](x: T)(implicit constr: ValueConstructor[T]): Value =
    constr.construct(x)

  def opt[T, V <: Value](x: Option[T])(implicit constr: ValueConstructor[T]): Value =
    constr.construct_opt(x)

  implicit val value2double: ValueExtractor[Double] = {
    case RealValue(Some(x)) => Some(x)
    case IntValue(Some(x)) => Some(x.toDouble)
    case LongValue(Some(x)) => Some(x.toDouble)
    case BoolValue(Some(x)) => Some(if (x) 1.0 else 0.0)
    case _ => None
  }

  implicit val value2int: ValueExtractor[Int] = {
    case RealValue(Some(x)) => Some(x.toInt)
    case IntValue(Some(x)) => Some(x)
    case LongValue(Some(x)) => Some(x.toInt)
    case BoolValue(Some(x)) => Some(if (x) 1 else 0)
    case _ => None
  }

  implicit val value2long: ValueExtractor[Long] = {
    case RealValue(Some(x)) => Some(x.round)
    case IntValue(Some(x)) => Some(x.toLong)
    case LongValue(Some(x)) => Some(x)
    case BoolValue(Some(x)) => Some(if (x) 1 else 0)
    case _ => None
  }

  implicit val value2bool: ValueExtractor[Boolean] = {
    case IntValue(Some(x)) => Some(x != 0)
    case LongValue(Some(x)) => Some(x != 0)
    case BoolValue(Some(x)) => Some(x)
    case _ => None
  }

  implicit val value2char: ValueExtractor[String] = {
    case RealValue(Some(x)) => Some(x.toString)
    case IntValue(Some(x)) => Some(x.toString)
    case LongValue(Some(x)) => Some(x.toString)
    case BoolValue(Some(x)) => Some(x.toString)
    case CharValue(Some(x)) => Some(x)
    case _ => None
  }

  implicit val value2date: ValueExtractor[Date] = {
    case DateValue(Some(value)) => Some(value)
    case _ => None
  }

  implicit val value2id: ValueExtractor[ObjectId] = {
    case IdValue(Some(value)) => Some(value)
    case _ => None
  }


  implicit val double2value: ValueConstructor[Double] = x => RealValue(Some(x))
  implicit val int2value: ValueConstructor[Int] = x => IntValue(Some(x))
  implicit val long2value: ValueConstructor[Long] = x => LongValue(Some(x))
  implicit val bool2value: ValueConstructor[Boolean] = x => BoolValue(Some(x))
  implicit val char2value: ValueConstructor[String] = x => CharValue(Some(x))
  implicit val date2value: ValueConstructor[Date] = x => DateValue(Some(x))
  implicit val id2value: ValueConstructor[ObjectId] = x => IdValue(Some(x))
}

