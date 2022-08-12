package sysmo.reform.shared.data

import java.util.Date

case object NoValue extends Value {
  override def is_set: Boolean = false
  override def not_set: Boolean = true
  override def _get: Option[Nothing] = None
}

trait ValueExtractor[T] {
  def extract(in: Value): Option[T]
}

class ValueImpl {
  def empty: Value = NoValue

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

  def apply[T](x: T)(implicit f: T => Value): Value = x
  //    constr.construct(x)
  //
  //  def opt[T](x: Option[T])(implicit constr: ValueConstructor[T]): Value =
  //    constr.construct_opt(x)



  object implicits {
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

    implicit def double2value(x: Double): RealValue = RealValue(Some(x))
    implicit def int2value(x: Int): IntValue = IntValue(Some(x))
    implicit def long2value(x: Long): LongValue = LongValue(Some(x))
    implicit def bool2value(x: Boolean): BoolValue = BoolValue(Some(x))
    implicit def char2value(x: String): CharValue = CharValue(Some(x))
    implicit def date2value(x: Date): DateValue = DateValue(Some(x))
    implicit def id2value(x: ObjectId): IdValue = IdValue(Some(x))
  }
}