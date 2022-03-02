package sysmo.reform.data

import org.apache.arrow.{vector => av}
import org.apache.arrow.memory.RootAllocator
import sysmo.reform.shared.data.table.{IncrementalVectorBuilder, VectorTypeclass}
import sysmo.reform.shared.data.{table => sdt}

package object table {

  object Implicits {
    implicit object ev_real extends VectorTypeclass[RootAllocator, sdt.RealVector] {
      type ValueType = Double
      type Storage = av.Float8Vector
      def empty_storage(allocator: Allocator, name: String): Storage = new av.Float8Vector(name, allocator)
      def get_incremental_builder(allocator: Allocator, name: String): IncrementalVectorBuilder[VecType, ValueType] =
        new ArrowRealIncrementalVectorBuilder(empty_storage(allocator, name))
    }

//    implicit object ev_int extends VectorTypeclass[RootAllocator, sdt.IntVector] {
//      type ValueType = Int
//      type Storage = av.IntVector
//      def empty_storage(allocator: Allocator, name: String): Storage = new av.IntVector(name, allocator)
//      def get_incremental_builder(allocator: Allocator, name: String): IncrementalVectorBuilder[VecType, ValueType] =
//        new ArrowRealIncrementalVectorBuilder(empty_storage(allocator, name))
//    }
  }

}
