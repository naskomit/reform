package sysmo.reform.data.table

import java.nio.charset.StandardCharsets

import sysmo.reform.shared.data.{table => sdt}
import org.apache.arrow.{vector => av}
import sysmo.reform.shared.data.table.TypedVector

//class ArrowIntIncrementalVectorBuilder(storage: av.IntVector) extends sdt.IncrementalVectorBuilder[sdt.IntVector, Int] {
//  override def append(value: ValueType): Unit = {
//    num_elements += 1
//    storage.setSafe(num_elements - 1, value)
//  }
//
//  override def toVector: VecType = {
//    storage.setValueCount(num_elements)
//    new ArrowIntVector(storage)
//  }
//}
//
//class ArrowRealIncrementalVectorBuilder(storage: av.Float8Vector) extends sdt.IncrementalVectorBuilder[sdt.RealVector, Double] {
//  override def append(value: ValueType): Unit = {
//    num_elements += 1
//    storage.setSafe(num_elements - 1, value)
//
//  }
//
//  override def toVector: VecType =  {
//    storage.setValueCount(num_elements)
//    new ArrowRealVector(storage)
//  }
//}
//
//class ArrowBoolIncrementalVectorBuilder(storage: av.BitVector) extends sdt.IncrementalVectorBuilder[sdt.BoolVector, Boolean] {
//  override def append(value: ValueType): Unit = {
//    num_elements += 1
//    storage.setSafe(num_elements - 1, if (value) 1 else 0)
//  }
//
//  override def toVector: VecType = {
//    storage.setValueCount(num_elements)
//    new ArrowBoolVector(storage)
//  }
//}
//
//class ArrowStringIncrementalVectorBuilder(storage: av.VarCharVector) extends sdt.IncrementalVectorBuilder[sdt.CharVector, String] {
//  override def append(value: ValueType): Unit = {
//    num_elements += 1
//    storage.setSafe(num_elements - 1, value.getBytes(StandardCharsets.UTF_8))
//  }
//
//  override def toVector: VecType = {
//    storage.setValueCount(num_elements)
//    new ArrowCharVector(storage)
//  }
//}

