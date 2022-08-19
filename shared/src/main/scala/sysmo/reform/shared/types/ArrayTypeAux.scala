package sysmo.reform.shared.types
import sysmo.reform.shared.data.ObjectId
import sysmo.reform.shared.expr.Expression

trait ArrayTypeAux {
  class Builder(protected[ArrayTypeAux] val _prototype: CompoundDataType) extends DataTypeBuilder[ArrayType]
  with HasLabelExprBuilder {

  }

  implicit class ArrayTypeImpl(builder: Builder) extends ArrayType {
    override def id: ObjectId = builder._id
    override def prototype: CompoundDataType = builder._prototype
    override def label_expr: Option[Expression] = builder._label_expr
    override def show: String = s"Array[${builder.prototype.symbol}]"
  }
}
