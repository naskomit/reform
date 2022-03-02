package sysmo.reform.shared.data.table


trait TableManager[Allct] extends AutoCloseable {
  type Allocator = Allct
//  def incremental_vector_builder[V, VecType <: Vector[V]]: IncrementalVectorBuilder[V, VecType]
  def incremental_vector_builder[VecT <: Vector[_]](name: String)(implicit tv: VectorTypeclass[Allocator, VecT]): IncrementalVectorBuilder[VecT, tv.ValueType]
}
