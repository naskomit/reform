package sysmo.reform.components.forms4.editors

import sysmo.reform.components.ReactAction
import sysmo.reform.components.forms4.ActionDispatcher
import sysmo.reform.shared.form4.{ArrayFieldId, ElementPath, FieldValue}
import sysmo.reform.shared.{form4 => F}

sealed trait EditorAction extends ReactAction
object EditorAction {
  type Dispatcher = ActionDispatcher[EditorAction]
}

sealed trait UpdateValue extends EditorAction
case class SetFieldValue(path: ElementPath, value : FieldValue[_]) extends UpdateValue

sealed trait GroupArrayAction extends EditorAction
case class RemoveArrayElement(path: ElementPath, id: ArrayFieldId) extends GroupArrayAction