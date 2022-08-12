package sysmo.reform.shared.types

import sysmo.reform.shared.expr.Expression
import sysmo.reform.shared.util.SequenceIndex

class RecordTypeImpl {
  class Builder(val symbol: String) extends DataTypeBuilder[RecordType] {
    var fields: Seq[RecordField] = Seq()
    var label_expr: Option[Expression] = None

//    def field()

    override def build: RecordType = new RecordType {
      val fields = builder.fields
      val field_index: SequenceIndex[String, RecordField] = new SequenceIndex(fields, _.name)
      override def symbol: String = builder.symbol
      override def field(name: String): Option[RecordField] = field_index.get(name)
      val label_expr: Option[Expression] = builder.label_expr
    }
  }

//  def apply(symbol: String): Builder = new Builder(symbol)
}

class RecordFieldImpl {
  class Builder(val name: String) extends DataTypeBuilder[RecordField] {
    var _label: Option[String] = None
    def label(v: String): this.type = {
      _label = Some(v)
      this
    }

    def build: RecordField = new RecordField {
      override def name: String = name
      val label: Option[String] = builder._label
      override def ftype: DataType = ???
      override def nullable: Boolean = ???
    }
  }
//  def apply(name: String)
}