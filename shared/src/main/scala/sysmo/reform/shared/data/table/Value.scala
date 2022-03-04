package sysmo.reform.shared.data.table

case class Value(v: Any) {
  def as_real: Option[Double] = v match {
    case v: Double => Some(v)
    case v: Int => Some(v)
    case _ => None
  }

  def as_int: Option[Int] = v match {
    case v: Double => Some(v.round.toInt)
    case v: Int => Some(v)
    case _ => None
  }

  def as_bool: Option[Boolean] = v match {
//    case v: Double => if (v == 0.0) Some(false) else Some(true)
    case v: Int => if (v == 0) Some(false) else Some(true)
    case v: Boolean => Some(v)
    case _ => None
  }

  def as_char: Option[String] = v match {
    case v: Double => Some(v.toString)
    case v: Int => Some(v.toString)
    case v: Boolean => Some(v.toString)
    case v: String => Some(v)
  }
}

