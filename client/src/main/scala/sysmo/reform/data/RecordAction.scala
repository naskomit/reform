package sysmo.reform.data

import sysmo.reform.components.editors.UpdateValue
trait RecordAction {

}

case class UpdateField[V](form_id : String, field_id : String, update: UpdateValue) extends RecordAction

