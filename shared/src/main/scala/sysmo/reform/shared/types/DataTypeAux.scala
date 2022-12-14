package sysmo.reform.shared.types

import sysmo.reform.shared.data.{ObjectId, UUIDSupplier}
import sysmo.reform.shared.expr.Expression

trait DataTypeAux[DT] {
  protected type Builder <: DataTypeBuilder[DT]
}

trait DataTypeBuilder[DT] {
  var dt: DT
  def builder: this.type = this
  def build: DT = dt
}
