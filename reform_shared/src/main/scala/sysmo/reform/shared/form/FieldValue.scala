package sysmo.reform.shared.form

import sysmo.reform.shared.{expr => E}
import sysmo.reform.shared.util.LabeledValue

sealed trait FieldValue[+V]
case object NoValue extends FieldValue[Nothing]
case object AllValues extends FieldValue[Nothing]
case class SomeValue[+V](v: LabeledValue[V]) extends FieldValue[V] {
  override def equals(obj: Any): Boolean = obj match {
    case SomeValue(v_other) => v.value == v_other.value
    case _ => false
  }

  override def hashCode(): Int = v.hashCode()
}
case class MultiValue[+V](v: Seq[LabeledValue[V]]) extends FieldValue[V]

object FieldValue {
  object implicits {
    implicit def str2expr(x: String): E.Constant = E.Constant(SomeValue(LabeledValue(x)))
    implicit def float2expr(x: Double): E.Constant = E.Constant(SomeValue(LabeledValue(x)))
    implicit def int2expr(x: Int): E.Constant = E.Constant(SomeValue(LabeledValue(x)))
    implicit def bool2expr(x: Boolean): E.Constant = E.Constant(SomeValue(LabeledValue(x)))
  }
}