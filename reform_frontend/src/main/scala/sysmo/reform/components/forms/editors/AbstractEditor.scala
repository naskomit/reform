package sysmo.reform.components.forms.editors

import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.form.{runtime => FR}

trait AbstractEditor[VT] extends AbstractFormComponent {
  type ValueType = VT
  type FieldValueType = FR.FieldValue[VT]

  trait Status
  case object Valid extends Status
  case class Error(msg: String, value: String) extends Status
  case class Warning(msg: String, value: String) extends Status
}
