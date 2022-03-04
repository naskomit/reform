package sysmo.reform.shared.data.table

case class FieldType(tpe: VectorType.Value, nullable: Boolean = true, metadata: Map[String, String] = Map())

case class Field(name: String, field_type: FieldType)

case class Schema(fields: Seq[Field], metadata: Map[String, String] = Map()) {
  val field_map = fields.map(x => (x.name, x)).toMap
  def field(index: Int): Field = fields(index)
  def field(name: String): Field = field_map(name)
}


