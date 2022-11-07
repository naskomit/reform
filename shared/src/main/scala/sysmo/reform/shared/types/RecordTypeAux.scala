package sysmo.reform.shared.types

import sysmo.reform.shared
import sysmo.reform.shared.data.ObjectId
import sysmo.reform.shared.expr.Expression
import sysmo.reform.shared.types
import sysmo.reform.shared.util.SequenceIndex

trait RecordTypeAux  extends DataTypeAux[RecordType] {
  class Builder(var dt: RecordType) extends DataTypeBuilder[RecordType] {

    def +(field: RecordFieldType): this.type = {
      dt = dt.copy(fields = dt.fields :+ field)
      this
    }

    def +(field: RecordFieldType.Builder): Builder = {
      this + field.build
    }

    def descr(v: String): this.type = {
      dt = dt.copy(descr = Some(v))
      this
    }

    def fields(field_list: RecordFieldType.Builder*): this.type = {
      dt = dt.copy(fields = dt.fields ++ field_list.map(f => f))
      this
    }

    def label_expr(expr: Expression): this.type = {
      dt = dt.copy(label_expr = Some(expr))
      this
    }

    def extend(f: Builder => Builder): this.type = {
      f(this)
      this
    }

  }

  implicit def builder2type(builder: Builder): RecordType = builder.build

  def apply(symbol: String): Builder =
    new Builder(new RecordType(symbol, None, Seq(), None))
}

trait RecordFieldTypeAux {
//  sealed trait FieldBuilder extends HasNameBuilder with HasLabelExprBuilder {
//    val _dtype: DataType
//    protected[types] var _optional: Boolean = true
//    def optional(v: Boolean): this.type = {
//      _optional = v
//      this
//    }
//  }
//
//  implicit class FieldImpl(builder: FieldBuilder) extends RecordFieldType {
//    override def name: String = builder._name
//    override def descr: Option[String] = builder._descr
//    override def optional: Boolean = builder._optional
//    override def label_expr: Option[Expression] = builder._label_expr
//    override def dtype: DataType = builder._dtype
//  }

  class Builder(var dt: RecordFieldType) {
    def optional(v: Boolean = true): this.type = {
      dt = dt.copy(optional = v)
      this
    }
    def label_expr(expr: Expression): this.type = {
      dt = dt.copy(label_expr = Some(expr))
      this
    }
    def build: RecordFieldType = dt
  }

  private def builder(name: String, dtype: DataType): Builder = {
    new Builder(new RecordFieldType(name, None, dtype, false, None))
  }

  implicit def builder2type(builder: Builder): RecordFieldType = builder.build

  trait Constr {
    def f_real(name: String): Builder = builder(name, PrimitiveDataType.Real)
    def f_int(name: String): Builder = builder(name, PrimitiveDataType.Int)
    def f_long(name: String): Builder = builder(name, PrimitiveDataType.Long)
    def f_char(name: String): Builder = builder(name, PrimitiveDataType.Char)
    def f_bool(name: String): Builder = builder(name, PrimitiveDataType.Bool)
    def f_date(name: String): Builder = builder(name, PrimitiveDataType.Date)
    def f_id(name: String): Builder = builder(name, PrimitiveDataType.Id)
    def f_compound(name: String, cmp: CompoundDataType): Builder =
      builder(name, cmp)
    def f_array(name: String, prototype: CompoundDataType): Builder =
      builder(name, ArrayType(prototype))

    def f_ref(name: String, prototype: CompoundDataType): Builder =
      builder(name, ReferenceType(prototype))

    def f_multi_ref(name: String, prototype: CompoundDataType): Builder =
      builder(name, MultiReferenceType(prototype))
  }

  object constr extends Constr

}