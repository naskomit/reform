package sysmo.reform.data.table

import sysmo.reform.shared.data.{table => sdt}
import sdt.{IncrementalVectorBuilder, VectorTypeclass, Vector, VectorType => VT}
import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.{vector => av}

class ArrowTableManager extends sdt.TableManager[RootAllocator] {
  val allocator = new RootAllocator(Long.MaxValue)
  override def incremental_vector_builder[VecT <: Vector[_]]
    (name: String)(implicit tv: VectorTypeclass[Allocator, VecT]): IncrementalVectorBuilder[VecT, tv.ValueType] =
    tv.get_incremental_builder(allocator, name)

//  def vector_from_seq[V](s : Seq[V]): Vector[V] = {
//    val builder = incremental_vector_builder[Vector[V]]()
//    builder
//  }

  override def close(): Unit = allocator.close()
}

object ArrowTableManager {
  def apply(): ArrowTableManager = new ArrowTableManager()
}