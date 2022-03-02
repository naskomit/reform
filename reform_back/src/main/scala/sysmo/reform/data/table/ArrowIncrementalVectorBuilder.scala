package sysmo.reform.data.table

import java.nio.charset.StandardCharsets

import sysmo.reform.shared.data.{table => sdt}
import org.apache.arrow.{vector => av}

class ArrowIntIncrementalVectorBuilder(storage: av.IntVector) extends sdt.IncrementalVectorBuilder[sdt.IntVector, Int] {
  override def append(value: ValueType): Unit = {
    num_elements += 1
    storage.setSafe(num_elements - 1, value)
  }

  override def toVector: VecType = {
    storage.setValueCount(num_elements)
    new ArrowIntVector(storage)
  }
}

class ArrowRealIncrementalVectorBuilder(storage: av.Float8Vector) extends sdt.IncrementalVectorBuilder[sdt.RealVector, Double] {
  override def append(value: ValueType): Unit = {
    num_elements += 1
    storage.setSafe(num_elements - 1, value)

  }

  override def toVector: VecType =  {
    storage.setValueCount(num_elements)
    new ArrowRealVector(storage)
  }
}

class ArrowBoolIncrementalVectorBuilder(storage: av.BitVector) extends sdt.IncrementalVectorBuilder[sdt.BoolVector, Boolean] {
  override def append(value: ValueType): Unit = {
    num_elements += 1
    storage.setSafe(num_elements - 1, if (value) 1 else 0)
  }

  override def toVector: VecType = {
    storage.setValueCount(num_elements)
    new ArrowBoolVector(storage)
  }
}

class ArrowStringIncrementalVectorBuilder(storage: av.VarCharVector) extends sdt.IncrementalVectorBuilder[sdt.CharVector, String] {
  override def append(value: ValueType): Unit = {
    num_elements += 1
    storage.setSafe(num_elements - 1, value.getBytes(StandardCharsets.UTF_8))
  }

  override def toVector: VecType = {
    storage.setValueCount(num_elements)
    new ArrowCharVector(storage)
  }
}

class ArrowVector[StorageT <: sdt.IStorage[ValueT], ValueT](storage: StorageT) extends sdt.Vector[ValueT] {
  def apply(i: Int): ValueType = storage.get(i)
  def length = storage.get_value_count
  val tpe = sdt.VectorType.Real
  override def range(start: Int, length: Int): VecT = {
    // TODO
    this
//    val tp = storage.getTransferPair(storage.getAllocator)
//    tp.splitAndTransfer(start, length)
//    new ArrowCharVector(tp.getTo.asInstanceOf[av.VarCharVector])
  }
  override def close(): Unit = storage.close()
}

class ArrowIncrementalVectorBuilder[StorageT <: sdt.IStorage[ValueT], ValueT](storage: StorageT) extends sdt.IncrementalVectorBuilder[sdt.Vector[ValueT], ValueT] {
  override def append(value: ValueType): Unit = {
    num_elements += 1
    storage.set_safe(num_elements - 1, value)
  }

  override def toVector: VecType = {
    storage.set_value_count(num_elements)
    new ArrowVector(storage)
  }
}