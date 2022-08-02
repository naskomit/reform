package sysmo.reform.shared.data.table

sealed trait Value[T] {
  val v: Option[T]

  def get: T = v.get
  def get_opt: Option[T] = v
  def get_or_else(x: T): T = v.getOrElse(x)

  def is_set: Boolean = v.isDefined
  def not_set: Boolean = v.isEmpty
  def is_na: Boolean = v.isEmpty

  def as_real: Option[Double] = None
  def as_int: Option[Int] = None
  def as_bool: Option[Boolean] = None
  def as_char: Option[String] = v.map(_.toString)
  def as_date: Option[java.util.Date] = None
  def equals(other: Value[_]): Boolean = {
    (v, other.v) match {
      case (Some(_v), Some(other_v)) => _v == other_v
      case (None, None) => true
      case _ => false
    }
  }
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


object Value {
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
  def empty(fieldType: FieldType): Value[_] = fieldType.tpe match {
    case VectorType.Int => int(None)
    case VectorType.Real => real(None)
    case VectorType.Bool => bool(None)
    case VectorType.Char => char(None)
  }
  def empty: Value[_] = IntValue(None)
}

