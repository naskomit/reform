package sysmo.reform.components.forms4.editors

import sysmo.reform.components.ReactAction
import sysmo.reform.components.forms4.ActionDispatcher
import sysmo.reform.shared.{form => F}

sealed trait EditorAction extends ReactAction
object EditorAction {
  type Dispatcher = ActionDispatcher[EditorAction]
}

sealed trait UpdateValue extends EditorAction
case class SetFieldValue(path: F.ElementPath, value : F.FieldValue[_]) extends UpdateValue

sealed trait GroupArrayAction extends EditorAction
case class RemoveArrayElement(array: F.GroupArray, id: F.ArrayFieldId) extends GroupArrayAction
case class InsertElementBefore(array: F.GroupArray, id: F.ArrayFieldId) extends GroupArrayAction
case class InsertElementAfter(array: F.GroupArray, id: F.ArrayFieldId) extends GroupArrayAction
case class AppendElement(array: F.GroupArray) extends GroupArrayAction
