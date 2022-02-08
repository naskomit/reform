package sysmo.coviddata.shared.data

import sysmo.coviddata.shared.data.PatientRecordMeta.FieldEnum


trait FieldType
case class StringType() extends FieldType
case class IntegerType() extends FieldType

case class RecordField(name: String, label: String, tpe: FieldType)

trait RecordMeta[U] {
  type FieldKey
  val field_keys: Seq[FieldKey]
  val fields : Map[FieldKey, RecordField]
  def field_key(name : String): FieldKey
  def get_value(obj : U, key : FieldKey): Any
  def update_value(obj : U, key : FieldKey, value : Any): U
}

trait RecordWithMeta[U] {
  val _meta: RecordMeta[U]
}


