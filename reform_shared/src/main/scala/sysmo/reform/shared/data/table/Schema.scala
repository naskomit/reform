package sysmo.reform.shared.data.table

import sysmo.reform.shared.data.table.Field.FieldBuilder
import sysmo.reform.shared.util.INamed

sealed trait ExtClass
case object Same extends ExtClass
case object Categorical extends ExtClass
case object Date extends ExtClass
case object DateTime extends ExtClass

case class FieldType(tpe: VectorType.Value, nullable: Boolean = true,
                     ext_class: ExtClass = Same, categories: Seq[String] = Seq(), metadata: Map[String, String] = Map())

case class Field(name: String, field_type: FieldType, label: Option[String] = None) extends INamed

object Field {
  class FieldBuilder(name: String, tpe: VectorType.Value) {
    var field: Field = Field(name, FieldType(tpe))
    def label(value: String): this.type = {
      field = field.copy(label = Some(value))
      this
    }
    def as(ext: ExtClass): this.type = {
      field = field.copy(field_type = field.field_type.copy(ext_class = ext))
      this
    }
    def build: Field = field
  }

  //  def apply(name: String, field_type: FieldType, label: Option[String] = None): Field = new Field(name, field_type, label)
//  def int(name: String): FieldBuilder = new FieldBuilder(Field(name, FieldType(VectorType.Int)))
//  def real(name: String): FieldBuilder = new FieldBuilder(Field(name, FieldType(VectorType.Real)))
//  def bool(name: String): FieldBuilder = new FieldBuilder(Field(name, FieldType(VectorType.Bool)))
//  def char(name: String): FieldBuilder = new FieldBuilder(Field(name, FieldType(VectorType.Char)))
}


case class Schema(name: String, label: Option[String], fields: Seq[Field], metadata: Map[String, String] = Map())
extends INamed {
  private val field_map = fields.zipWithIndex.map({case (field, index) => (field.name, index)}).toMap
  def field(index: Int): Option[Field] = if (index < fields.length) Some(fields(index)) else None
  def field(name: String): Option[Field] = field_index(name).map(index => fields(index))
  def field_index(name: String): Option[Int] = field_map.get(name)

  def modify(f: Schema.Builder => Schema.Builder): Schema = {
    val builder = new Schema.Builder(this)
    f(builder)
    builder.build
  }
}

object Schema {
  class Builder(name: String) {
    def this(s: Schema) = {
      this(s.name)
      this._label = s.label
      this.fields = s.fields
    }

    var _label: Option[String] = None
    private var fields = Seq[Field]()
    def label(v: String): this.type = {
      _label = Some(v)
      this
    }
    def field(name: String, tpe: VectorType.Value): this.type = {
      val builder = new Field.FieldBuilder(name, tpe)
      fields = fields :+ builder.build
      this
    }
    def field(name: String, tpe: VectorType.Value, b: FieldBuilder => _): this.type = {
      val builder = new Field.FieldBuilder(name, tpe)
      b(builder)
      fields = fields :+ builder.build
      this
    }

    def int(name: String): this.type = field(name, VectorType.Int)
    def int(name: String, b: FieldBuilder => _): this.type = field(name, VectorType.Int, b)
    def real(name: String): this.type = field(name, VectorType.Real)
    def real(name: String, b: FieldBuilder => _): this.type = field(name, VectorType.Real, b)
    def bool(name: String): this.type = field(name, VectorType.Bool)
    def bool(name: String, b: FieldBuilder => _): this.type = field(name, VectorType.Bool, b)
    def char(name: String): this.type = field(name, VectorType.Char)
    def char(name: String, b: FieldBuilder => _): this.type = field(name, VectorType.Char, b)

    def build: Schema = Schema(name, _label, fields)
  }
  def builder(name: String) : Builder = new Builder(name)
}
