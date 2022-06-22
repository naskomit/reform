package sysmo.reform.shared.form.runtime

sealed trait FormAction

  //object EditorAction {
  //  type Dispatcher = ActionDispatcher[EditorAction]
  //}

sealed trait UpdateValue extends FormAction
case class SetFieldValue(id: ObjectId, value : FieldValue[_]) extends UpdateValue

sealed trait GroupArrayAction extends FormAction
case class RemoveArrayElement(array: Array, id: ObjectId) extends GroupArrayAction
case class InsertElementBefore(array: Array, id: ObjectId) extends GroupArrayAction
case class InsertElementAfter(array: Array, id: ObjectId) extends GroupArrayAction
case class AppendElement(array: ObjectId) extends GroupArrayAction
