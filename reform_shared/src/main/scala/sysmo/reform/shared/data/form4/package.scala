package sysmo.reform.shared.data

package object form4 {
  type CharValue = FieldValue[String]
  type IntValue = FieldValue[Int]
  type FloatValue = FieldValue[Double]
  type BoolValue = FieldValue[Boolean]
  type RecValue = FieldValue[ValueMap]

  implicit class CharFV(val v: String) extends FVConverter[String]
  implicit class IntFV(val v: Int) extends FVConverter[Int]
  implicit class FloatFV(val v: Double) extends FVConverter[Double]
  implicit class BoolFV(val v: Boolean) extends FVConverter[Boolean]
  implicit class RecFV(val v: ValueMap) extends FVConverter[ValueMap]

}
