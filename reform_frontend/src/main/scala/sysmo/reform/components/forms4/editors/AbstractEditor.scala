package sysmo.reform.components.forms4.editors

import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.form4.FieldValue
import sysmo.reform.shared.{form4 => F}

trait AbstractEditor[VT] extends ReactComponent {
  type ValueType = VT
  type FieldValueType = FieldValue[VT]

  trait Status
  case object Valid extends Status
  case class Error(msg: String, value: String) extends Status
  case class Warning(msg: String, value: String) extends Status
}
