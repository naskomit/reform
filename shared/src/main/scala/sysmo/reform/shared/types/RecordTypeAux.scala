package sysmo.reform.shared.types

import sysmo.reform.shared
import sysmo.reform.shared.data.ObjectId
import sysmo.reform.shared.expr.Expression
import sysmo.reform.shared.types
import sysmo.reform.shared.util.SequenceIndex

trait RecordTypeAux {
  class Builder(val symbol: String) extends DataTypeBuilder[RecordType]
    with HasSymbolBuilder {
    protected[RecordTypeAux] var _fields: Seq[RecordFieldType] = Seq()
    protected[RecordTypeAux] var _field_index: SequenceIndex[String, RecordFieldType] = new SequenceIndex(_fields, _.name)

    def +(field: RecordFieldType.FieldBuilder): this.type = {
      _fields = _fields :+ field
      _field_index = new SequenceIndex(_fields, _.name)
      this
    }

    protected[RecordTypeAux] var label_expr: Option[Expression] = None
    def label_expr(expr: Expression): this.type = {
      label_expr = Some(expr)
      this
    }

    def extend(f: Builder => Builder): this.type = {
      f(this)
      this
    }

  }

  implicit class RecordTypeImpl(builder: Builder) extends RecordType {
    override def id: ObjectId = builder._id
    override def symbol: String = builder.symbol
    override def descr: Option[String] = builder._descr
    override def fields: Seq[RecordFieldType] = builder._fields
    override def field(name: String): Option[RecordFieldType] = builder._field_index.get(name)
    override def field_index(name: String): Option[Int] = builder._field_index.get_index(name)
    override def label_expr: Option[Expression] = builder.label_expr
  }

  def apply(symbol: String): Builder = new Builder(symbol)
}

trait RecordFieldTypeAux {
  sealed trait FieldBuilder extends HasNameBuilder with HasLabelExprBuilder {
    val _dtype: DataType
    protected[types] var _optional: Boolean = true
    def optional(v: Boolean): this.type = {
      _optional = v
      this
    }
  }

  implicit class FieldImpl(builder: FieldBuilder) extends RecordFieldType {
    override def name: String = builder._name
    override def descr: Option[String] = builder._descr
    override def optional: Boolean = builder._optional
    override def label_expr: Option[Expression] = builder._label_expr
    override def dtype: DataType = builder._dtype
  }

  trait Constr {
    def f_char(name: String): FieldBuilder =
      new FieldBuilder {
        override val _name: String = name
        override val _dtype: DataType = AtomicDataType.Char
      }

    def f_int(name: String): FieldBuilder =
      new FieldBuilder {
        override val _name: String = name
        override val _dtype: DataType = AtomicDataType.Int
      }

    def f_real(name: String): FieldBuilder =
      new FieldBuilder {
        override val _name: String = name
        override val _dtype: DataType = AtomicDataType.Real
      }

    def f_compound(name: String, cmp: CompoundDataType): FieldBuilder =
      new FieldBuilder {
        override val _name: String = name
        override val _dtype: DataType = cmp
      }

    def f_array(name: String, prototype: CompoundDataType): FieldBuilder =
      new FieldBuilder {
        override val _name: String = name
        override val _dtype: DataType = new ArrayType.Builder(prototype)
      }

    def f_ref(name: String, prototype: CompoundDataType): FieldBuilder =
      new FieldBuilder {
        override val _name: String = name
        override val _dtype: DataType = new ReferenceType.Builder(prototype)
      }

    def f_multi_ref(name: String, prototype: CompoundDataType): FieldBuilder =
      new FieldBuilder {
        override val _name: String = name
        override val _dtype: DataType = new MultiReferenceType.Builder(prototype)
      }
  }

}