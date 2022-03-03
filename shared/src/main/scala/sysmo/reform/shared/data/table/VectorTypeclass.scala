package sysmo.reform.shared.data.table

// Type class


object VectorType extends Enumeration {
  type VectorType = Value
  val Int, Real, Bool, Char = Value
}

trait VectorTypeclass[V] {
  type ValueType = V
  type Storage <: VectorStorage[ValueType]
  val tpe: VectorType.VectorType
  def create_storage(manager: TableManager, name: String): Storage
}
