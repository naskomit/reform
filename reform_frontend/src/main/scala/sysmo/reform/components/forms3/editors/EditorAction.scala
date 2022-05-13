package sysmo.reform.components.forms3.editors

import sysmo.reform.components.ReactAction
import sysmo.reform.components.forms3.ActionDispatcher
import sysmo.reform.shared.data.form3.{ElementPath, FormData => FD}

trait EditorAction extends ReactAction
object EditorAction {
  type Dispatcher = ActionDispatcher[EditorAction]
}

trait UpdateValue extends EditorAction
case class SetFieldValue(path: ElementPath, value : FD.FieldValue[_]) extends UpdateValue

case class GotFocus() extends EditorAction
case class LostFocus() extends EditorAction