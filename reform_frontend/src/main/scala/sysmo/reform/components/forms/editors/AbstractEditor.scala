package sysmo.reform.components.forms.editors

import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}

trait AbstractEditor[FBT <: FB.AtomicField] extends AbstractFormComponent[FR.AtomicValue, FBT] {
  type VT = FBT#ValueType
  type FieldValueType = FR.FieldValue[VT]
  trait Status

  case object Valid extends Status
  case class Error(msg: String, value: String) extends Status
  case class Warning(msg: String, value: String) extends Status

  implicit class PropOps(p: Props) {
    def value: FieldValueType = p.obj.value.asInstanceOf[FieldValueType]
    def field: FieldBuildType = p.obj.prototype.asInstanceOf[FieldBuildType]
  }
}
