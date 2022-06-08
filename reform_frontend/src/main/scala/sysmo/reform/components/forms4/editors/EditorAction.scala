package sysmo.reform.components.forms4.editors

import sysmo.reform.components.ReactAction
import sysmo.reform.components.forms4.ActionDispatcher
import sysmo.reform.shared.data.{form4 => F}

sealed trait EditorAction extends ReactAction
object EditorAction {
  type Dispatcher = ActionDispatcher[EditorAction]
}

sealed trait UpdateValue extends EditorAction
case class SetFieldValue(path: F.ElementPath, value : F.FieldValue[_]) extends UpdateValue

sealed trait GroupArrayAction extends EditorAction
case class RemoveArrayElement(path: F.ElementPath, id: F.ArrayFieldId) extends GroupArrayAction