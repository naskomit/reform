package sysmo.reform.shared.types

import sysmo.reform.shared.data.{ObjectId, UUIDSupplier}
import sysmo.reform.shared.expr.Expression

trait HasNameBuilder {
  val _name: String
  protected[types] var _descr: Option[String] = None
  def descr(v: String): this.type = {
    _descr = Some(v)
    this
  }
}

trait HasLabelExprBuilder {
  protected[types] var _label_expr: Option[Expression] = None
  def label_expr(expr: Expression): this.type = {
    _label_expr = Some(expr)
    this
  }
}

trait DataTypeBuilder[DT] {
  val _id: ObjectId = DataTypeAux.IdSupplier.new_id
  def builder: this.type = this
//  def build: DT
}

object DataTypeAux {
  object IdSupplier extends UUIDSupplier
}