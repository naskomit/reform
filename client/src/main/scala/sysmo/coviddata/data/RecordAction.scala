package sysmo.coviddata.data

trait RecordAction {

}

case class UpdateField[U](form_id : String, field_id : String, value : U) extends RecordAction

