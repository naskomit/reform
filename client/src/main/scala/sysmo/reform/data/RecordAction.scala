package sysmo.reform.data

import sysmo.reform.components.editors.UpdateValue
import sysmo.reform.shared.data.{FieldValue, Record}

trait RecordAction {

}

case class UpdateField[V](form_id : String, field_id : String, update: UpdateValue) extends RecordAction

