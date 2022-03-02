package sysmo.reform.shared.data.table

// Type class

trait IStorage[ValueT] {
  type SType
  def write(v: ValueT): SType
  def set_safe(index: Int, v: ValueT)
  def get(index: Int): ValueT
  def set_value_count(i: Int): Unit
  def get_value_count: Int
  def close(): Unit
}

trait VectorTypeclass[Allct, VecT <: Vector[_]] {
  type ValueType
  type VecType = VecT
  type Storage //<: IStorage[ValueType]
  type Allocator = Allct
  def empty_storage(allocator: Allocator, name: String): Storage
  def get_incremental_builder(allocator: Allocator, name: String): IncrementalVectorBuilder[VecType, ValueType]
}
