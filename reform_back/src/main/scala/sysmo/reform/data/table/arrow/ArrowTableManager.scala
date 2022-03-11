package sysmo.reform.data.table.arrow

import org.apache.arrow.memory.RootAllocator
import sysmo.reform.util.Logging
import scala.collection.mutable
import sysmo.reform.shared.data.{table => sdt}

class ArrowTableManager extends sdt.TableManager with Logging{
  override type Allocator = RootAllocator
  override val allocator = new RootAllocator(Int.MaxValue)
  private val allocated = mutable.Set[sdt.VectorStorage[_]]()

  override def incremental_series_builder(field: sdt.Field): sdt.SeriesBuilder = {
    import ArrowVector._
    val vec_builder = field.field_type.tpe match {
      case sdt.VectorType.Real => incremental_vector_builder[Double](field.name)
      case sdt.VectorType.Int => incremental_vector_builder[Int](field.name)
      case sdt.VectorType.Bool => incremental_vector_builder[Boolean](field.name)
      case sdt.VectorType.Char => incremental_vector_builder[String](field.name)
    }
    new sdt.SeriesBuilderImpl(field, vec_builder)
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

  override def on_allocate_storage(storage: sdt.VectorStorage[_]): Unit = {
    allocated.add(storage)
  }
  override def on_free_storage(storage: sdt.VectorStorage[_]): Unit = {
    allocated.remove(storage)
  }
}

object ArrowTableManager {
  def apply(): ArrowTableManager = new ArrowTableManager()
}