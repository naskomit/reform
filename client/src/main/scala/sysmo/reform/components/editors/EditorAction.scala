package sysmo.reform.components.editors

import sysmo.reform.components.ReactAction
import sysmo.reform.shared.data.FieldValue

trait EditorAction extends ReactAction

trait UpdateValue extends EditorAction
case class SetValue(value : FieldValue) extends UpdateValue

case class GotFocus() extends EditorAction
case class LostFocus() extends EditorAction