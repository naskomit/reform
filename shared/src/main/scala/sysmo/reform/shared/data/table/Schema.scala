package sysmo.reform.shared.data.table

import sysmo.reform.shared.util.{INamed}

sealed trait ExtClass
case object Same extends ExtClass
case object Categorical extends ExtClass
case object Date extends ExtClass
case object DateTime extends ExtClass

case class FieldType(tpe: VectorType.Value, nullable: Boolean = true,
                     ext_class: ExtClass = Same, categories: Seq[String] = Seq(), metadata: Map[String, String] = Map())

case class Field(name: String, field_type: FieldType, label: Option[String] = None) extends INamed

case class FieldBuilder(field: Field) {
  def get: Field = field
  def label(value: String): FieldBuilder = FieldBuilder(field.copy(label = Some(value)))
}

object Field {
//  def apply(name: String, field_type: FieldType, label: Option[String] = None): Field = new Field(name, field_type, label)
  def int(name: String): FieldBuilder = FieldBuilder(Field(name, FieldType(VectorType.Int)))
  def real(name: String): FieldBuilder = FieldBuilder(Field(name, FieldType(VectorType.Real)))
  def bool(name: String): FieldBuilder = FieldBuilder(Field(name, FieldType(VectorType.Bool)))
  def char(name: String): FieldBuilder = FieldBuilder(Field(name, FieldType(VectorType.Char)))
}


case class Schema(fields: Seq[Field], metadata: Map[String, String] = Map()) {
  private val field_map = fields.zipWithIndex.map({case (field, index) => (field.name, index)}).toMap
  def field(index: Int): Option[Field] = if (index < fields.length) Some(fields(index)) else None
  def field(name: String): Option[Field] = field_index(name).map(index => fields(index))
  def field_index(name: String): Option[Int] = field_map.get(name)
}


