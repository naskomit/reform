package sysmo.reform.shared.data.table


trait TableManager extends AutoCloseable {
  type Allocator <: AutoCloseable
  val allocator: Allocator

  def on_allocate_storage(storage: VectorStorage[_]): Unit = {}
  def on_free_storage(storage: VectorStorage[_]): Unit = {}

  def create_mutable_storage[V](name: String)(implicit tv: VectorTypeclass[V]): tv.MutableStorage = {
    val storage = tv.create_mutable_storage(this, name)
    on_allocate_storage(storage)
    storage
  }

  def incremental_vector_builder[V](name: String)(implicit tv: VectorTypeclass[V]):
    IncrementalVectorBuilder[V, tv.MutableStorage] = new IncrementalVectorBuilder[V, tv.MutableStorage](
    create_mutable_storage[V](name)(tv))

  def incremental_series_builder(field: Field): SeriesBuilder

  def incremental_table_builder(schema: Schema): TableBuilder = {
    val col_builders = schema.fields.map(x => incremental_series_builder(x))
    new IncrementalTableBuilder(schema, col_builders)
  }

  def vec_from[V](seq: Seq[V], name: String)(implicit tv: VectorTypeclass[V]): Vector[V, VectorStorage[V]] = {
    val builder = incremental_vector_builder[V](name)
    seq.foreach {
      case null => builder :+ None
      case x => builder :+ Some(x)
    }
    builder.toVector
  }


}
