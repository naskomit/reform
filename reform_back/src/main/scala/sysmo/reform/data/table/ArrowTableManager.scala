package sysmo.reform.data.table

import sysmo.reform.shared.data.{table => sdt}
import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.{vector => av}
import sysmo.reform.shared.data.table.{Field, Table, TableBuilder, VectorStorage}
import sysmo.reform.util.Logging

import scala.collection.mutable

class ArrowTableManager extends sdt.TableManager with Logging{
  override type Allocator = RootAllocator
  override val allocator = new RootAllocator(Int.MaxValue)
  private val allocated = mutable.Set[sdt.VectorStorage[_]]()

  override def incremental_vector_builder[V](name: String)(implicit tv: sdt.VectorTypeclass[V]):
      sdt.IncrementalVectorBuilder[V, tv.Storage] =
      new sdt.IncrementalVectorBuilder[V, tv.Storage](
        create_storage[V](name)(tv))

  override def incremental_series_builder(field: sdt.Field): sdt.SeriesBuilder = {
    import sysmo.reform.data.table.ArrowVector._
    val vec_builder = field.field_type.tpe match {
      case sdt.VectorType.Real => incremental_vector_builder[Double](field.name)
      case sdt.VectorType.Int => incremental_vector_builder[Int](field.name)
      case sdt.VectorType.Bool => incremental_vector_builder[Boolean](field.name)
      case sdt.VectorType.Char => incremental_vector_builder[String](field.name)
    }
    new sdt.SeriesBuilderImpl(field, vec_builder)
  }


  override def incremental_table_builder(schema: sdt.Schema): TableBuilder = {
    val col_builders = schema.fields.map(x => incremental_series_builder(x))
    new sdt.IncrementalTableBuilder(schema, col_builders)
  }

  override def close(): Unit = {
    if (allocated.nonEmpty) {
      logger.warn(f"${allocated.size} elements left allocated. De-allocating!")
      for (el <- allocated) {
        allocated.remove(el)
        el.close()

      }
    }
    allocator.close()
  }

  override def on_allocate_storage(storage: VectorStorage[_]): Unit = {
    allocated.add(storage)
  }
  override def on_free_storage(storage: VectorStorage[_]): Unit = {
    allocated.remove(storage)
  }
}

object ArrowTableManager {
  def apply(): ArrowTableManager = new ArrowTableManager()
}