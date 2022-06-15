package sysmo.reform.components.forms.editors

import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.{form => F}

trait AbstractEditor[VT] extends AbstractFormComponent {
  type ValueType = VT
  type FieldValueType = F.FieldValue[VT]

  trait Status
  case object Valid extends Status
  case class Error(msg: String, value: String) extends Status
  case class Warning(msg: String, value: String) extends Status
}
