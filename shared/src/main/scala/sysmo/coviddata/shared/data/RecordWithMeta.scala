package sysmo.coviddata.shared.data


trait FieldType
case class StringType() extends FieldType
case class IntegerType() extends FieldType

case class RecordField(name: String, label: String, tpe: FieldType)

trait RecordMeta {
  type FieldKey
  val field_keys: Seq[FieldKey]
  val fields : Map[FieldKey, RecordField]
}

trait RecordWithMeta[T] {
  val _meta: RecordMeta
}


