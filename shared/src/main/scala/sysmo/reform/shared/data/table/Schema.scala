package sysmo.reform.shared.data.table

case class FieldType(tpe: VectorType.Value, nullable: Boolean = true, metadata: Map[String, String] = Map())

case class Field(name: String, field_type: FieldType, label: Option[String] = None)

case class Schema(fields: Seq[Field], metadata: Map[String, String] = Map()) {
  private val field_map = fields.zipWithIndex.map({case (field, index) => (field.name, index)}).toMap
  def field(index: Int): Field = fields(index)
  def field(name: String): Field = fields(field_index(name))
  def field_index(name: String): Int = field_map(name)
}


