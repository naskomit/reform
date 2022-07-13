package sysmo.reform.shared.form.runtime

sealed trait FormAction

  //object EditorAction {
  //  type Dispatcher = ActionDispatcher[EditorAction]
  //}

sealed trait UpdateValue extends FormAction
case class SetFieldValue(id: ObjectId, value : FieldValue[_]) extends UpdateValue

sealed trait GroupArrayAction extends FormAction
case class RemoveElement(array_id: ObjectId, id: ObjectId) extends GroupArrayAction
case class InsertElementBefore(array_id: ObjectId, id: ObjectId, concrete_type: Option[String]) extends GroupArrayAction
case class InsertElementAfter(array_id: ObjectId, id: ObjectId, concrete_type: Option[String]) extends GroupArrayAction
case class AppendElement(array_id: ObjectId, concrete_type: Option[String] = None) extends GroupArrayAction
