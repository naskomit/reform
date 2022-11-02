package sysmo.reform.shared.data

import java.util.Date

case object NoValue extends Value {
  override def is_set: Boolean = false
  override def not_set: Boolean = true
  override def _get: Option[Nothing] = None
  override def show(formatter: Option[ValueFormatter]): String = "N/A"
}

trait ValueConstructor[-T] {
  def toValue(raw: T): Value
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
    def show(formatter: Option[ValueFormatter]): String =
      v.map("%.3f".format(_)).getOrElse(NoValue.show(None))
  }

  case class IntValue(v: Option[Int]) extends ValueFromOption {
    override def _get: Option[Int] = v
    def show(formatter: Option[ValueFormatter]): String =
      v.map("%d".format(_)).getOrElse(NoValue.show(None))
  }

  case class LongValue(v: Option[Long]) extends ValueFromOption {
    override def _get: Option[Long] = v
    def show(formatter: Option[ValueFormatter]): String =
      v.map("%d".format(_)).getOrElse(NoValue.show(None))
  }

  case class CharValue(v: Option[String]) extends ValueFromOption {
    override def _get: Option[String] = v
    def show(formatter: Option[ValueFormatter]): String =
      v.map("%s".format(_)).getOrElse(NoValue.show(None))
  }

  case class BoolValue(v: Option[Boolean]) extends ValueFromOption {
    override def _get: Option[Boolean] = v
    def show(formatter: Option[ValueFormatter]): String =
      v.map("%s".format(_)).getOrElse(NoValue.show(None))
  }

  case class DateValue(v: Option[Date]) extends ValueFromOption {
    override def _get: Option[Date] = v
    def show(formatter: Option[ValueFormatter]): String =
      v.map("%s".format(_)).getOrElse(NoValue.show(None))
  }

  case class IdValue(v: Option[ObjectId]) extends ValueFromOption {
    override def _get: Option[ObjectId] = v
    def show(formatter: Option[ValueFormatter]): String =
      v.map("%s".format(_)).getOrElse(NoValue.show(None))
  }

  def apply[T](x: T)(implicit vc: ValueConstructor[T]): Value = {
    if (x == null)
      Value.empty
    else
      vc.toValue(x)
  }

  def from_any(x: Any): Value = {
    import implicits._
    x match {
      case x: Double => Value(x)
      case x: Int => Value(x)
      case x: Long => Value(x)
      case x: Boolean => Value(x)
      case x: String => Value(x)
      case x: Date => Value(x)
      case x: ObjectId => Value(x)
      case _ => Value.empty
    }
  }

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

    implicit val double2value: ValueConstructor[Double] = x => RealValue(Some(x))
    implicit val int2value: ValueConstructor[Int] = x => IntValue(Some(x))
    implicit val long2value: ValueConstructor[Long] = x => LongValue(Some(x))
    implicit val bool2value: ValueConstructor[Boolean] = x => BoolValue(Some(x))
    implicit val char2value: ValueConstructor[String] = x => CharValue(Some(x))
    implicit val date2value: ValueConstructor[Date] = x => DateValue(Some(x))
    implicit val id2value: ValueConstructor[ObjectId] = x => IdValue(Some(x))
  }
}