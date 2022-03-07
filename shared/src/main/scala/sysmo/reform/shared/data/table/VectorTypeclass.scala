package sysmo.reform.shared.data.table

// Type class


object VectorType extends Enumeration {
  type VectorType = Value
  val Int, Real, Bool, Char = Value
}

trait VectorTypeclass[V] {
  type ValueType = V
  type MutableStorage <: VectorStorage[ValueType]
  type ImmutableStorage <: VectorStorage[ValueType]
  val tpe: VectorType.VectorType
  def create_mutable_storage(manager: TableManager, name: String): MutableStorage
}
