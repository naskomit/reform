package sysmo.reform.shared.types
import scala.language.implicitConversions
import sysmo.reform.shared.expr.Expression

trait ArrayTypeAux extends DataTypeAux[ArrayType] {
  class Builder(var dt: ArrayType) extends DataTypeBuilder[ArrayType] {
    def label_expr(expr: Expression): this.type = {
      dt = dt.copy(label_expr = Some(expr))
      this
    }
  }

  implicit def builder2type(builder: Builder): ArrayType = builder.build

  def apply(prototype: CompoundDataType): Builder =
    new Builder(new ArrayType(prototype, None))
}
