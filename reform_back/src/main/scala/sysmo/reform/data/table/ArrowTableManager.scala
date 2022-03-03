package sysmo.reform.data.table

import sysmo.reform.shared.data.{table => sdt}
import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.{vector => av}
import sysmo.reform.shared.data.table.VectorStorage
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