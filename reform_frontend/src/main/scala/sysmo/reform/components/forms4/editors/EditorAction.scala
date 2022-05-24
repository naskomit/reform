package sysmo.reform.components.forms4.editors

import sysmo.reform.components.ReactAction
import sysmo.reform.components.forms4.ActionDispatcher
import sysmo.reform.shared.data.{form4 => F}

trait EditorAction extends ReactAction
object EditorAction {
  type Dispatcher = ActionDispatcher[EditorAction]
}

trait UpdateValue extends EditorAction
case class SetFieldValue(path: F.ElementPath, value : F.FieldValue[_]) extends UpdateValue

case class GotFocus() extends EditorAction
case class LostFocus() extends EditorAction