package sysmo.reform.shared.data.table


trait TableManager extends AutoCloseable {
  type Allocator
  val allocator: Allocator

  def on_allocate_storage(storage: VectorStorage[_]): Unit = {}
  def on_free_storage(storage: VectorStorage[_]): Unit = {}

  def create_storage[V](name: String)(implicit tv: VectorTypeclass[V]): tv.Storage = {
    val storage = tv.create_storage(this, name)
    on_allocate_storage(storage)
    storage
  }

  def incremental_vector_builder[V](name: String)(implicit tv: VectorTypeclass[V]):
    IncrementalVectorBuilder[V, tv.Storage]

  def incremental_series_builder(field: Field): SeriesBuilder

  def incremental_table_builder(schema: Schema): TableBuilder

  def vec_from[V](seq: Seq[V], name: String)(implicit tv: VectorTypeclass[V]): Vector[V, tv.Storage] = {
    val builder = incremental_vector_builder[V](name)
    for (el <- seq) {
      builder :+ el
    }
    builder.toVector
  }
}
