package sysmo.reform.data.table

import org.apache.arrow.{vector => av}
import sysmo.reform.shared.data.{table => sdt}
import java.nio.charset.StandardCharsets

import org.apache.arrow.memory.BufferAllocator

object ArrowVector {
  type RootAllocator = org.apache.arrow.memory.RootAllocator

  implicit object ev_real extends sdt.VectorTypeclass[Double] {
    type StorageBackend = av.Float8Vector
    val tpe = sdt.VectorType.Real
    class Storage(manager: ArrowTableManager, backend: StorageBackend) extends sdt.VectorStorage[ValueType](manager) {
      override def set_safe(index: Int, v: ValueType): Unit = backend.setSafe(index, v)
      override def get(index: Int): ValueType = backend.get(index)
      override def set_value_count(i: Int): Unit = backend.setValueCount(i)
      override def get_value_count: Int = backend.getValueCount
      override def range_view(start: Int, length: Int): Storage = {
        val tp = backend.getTransferPair(backend.getAllocator)
        tp.splitAndTransfer(start, length)
        new Storage(manager, tp.getTo.asInstanceOf[StorageBackend])
      }
      override def close(): Unit = backend.close()
    }
    override def create_storage(manager: sdt.TableManager, name: String): Storage = {
      val mngr = manager.asInstanceOf[ArrowTableManager]
      new Storage(mngr, new StorageBackend(name, mngr.allocator))
    }
  }

  implicit object ev_int extends sdt.VectorTypeclass[Int] {
    type StorageBackend = av.IntVector
    val tpe = sdt.VectorType.Int
    class Storage(manager: ArrowTableManager, backend: StorageBackend) extends sdt.VectorStorage[ValueType](manager) {
      override def set_safe(index: Int, v: ValueType): Unit = backend.setSafe(index, v)
      override def get(index: Int): ValueType = backend.get(index)
      override def set_value_count(i: Int): Unit = backend.setValueCount(i)
      override def get_value_count: Int = backend.getValueCount
      override def range_view(start: Int, length: Int): Storage = {
        val tp = backend.getTransferPair(backend.getAllocator)
        tp.splitAndTransfer(start, length)
        new Storage(manager, tp.getTo.asInstanceOf[StorageBackend])
      }
      override def close(): Unit = backend.close()
    }
    override def create_storage(manager: sdt.TableManager, name: String): Storage = {
      val mngr = manager.asInstanceOf[ArrowTableManager]
      new Storage(mngr, new StorageBackend(name, mngr.allocator))
    }
  }

  implicit object ev_bool extends sdt.VectorTypeclass[Boolean] {
    type StorageBackend = av.BitVector
    val tpe = sdt.VectorType.Bool
    class Storage(manager: ArrowTableManager, backend: StorageBackend) extends sdt.VectorStorage[ValueType](manager) {
      override def set_safe(index: Int, v: ValueType): Unit = backend.setSafe(index, if (v) 1 else 0)
      override def get(index: Int): ValueType = backend.get(index) == 1
      override def set_value_count(i: Int): Unit = backend.setValueCount(i)
      override def get_value_count: Int = backend.getValueCount
      override def range_view(start: Int, length: Int): Storage = {
        val tp = backend.getTransferPair(backend.getAllocator)
        tp.splitAndTransfer(start, length)
        new Storage(manager, tp.getTo.asInstanceOf[StorageBackend])
      }
      override def close(): Unit = backend.close()
    }
    override def create_storage(manager: sdt.TableManager, name: String): Storage = {
      val mngr = manager.asInstanceOf[ArrowTableManager]
      new Storage(mngr, new StorageBackend(name, mngr.allocator))
    }
  }

  implicit object ev_char extends sdt.VectorTypeclass[String] {
    type StorageBackend = av.VarCharVector
    val tpe = sdt.VectorType.Char
    class Storage(manager: ArrowTableManager, backend: StorageBackend) extends sdt.VectorStorage[ValueType](manager) {
      override def set_safe(index: Int, v: ValueType): Unit = backend.setSafe(index, v.getBytes(StandardCharsets.UTF_8))
      override def get(index: Int): ValueType = new String(backend.get(index), StandardCharsets.UTF_8)
      override def set_value_count(i: Int): Unit = backend.setValueCount(i)
      override def get_value_count: Int = backend.getValueCount
      override def range_view(start: Int, length: Int): Storage = {
        val tp = backend.getTransferPair(backend.getAllocator)
        tp.splitAndTransfer(start, length)
        new Storage(manager, tp.getTo.asInstanceOf[StorageBackend])
      }
      override def close(): Unit = backend.close()
    }
    override def create_storage(manager: sdt.TableManager, name: String): Storage = {
      val mngr = manager.asInstanceOf[ArrowTableManager]
      new Storage(mngr, new StorageBackend(name, mngr.allocator))
    }
  }
}



//import java.nio.charset.StandardCharsets
//
//import org.apache.arrow.memory.RootAllocator
//import sysmo.reform.data.table.Implicits.ev_real.ValueType
//import sysmo.reform.shared.data.table.VectorTypeclass
//import sysmo.reform.shared.data.{table => sdt}
//import org.apache.arrow.{vector => av} //{VarCharVector, IntVector, VectorSchemaRoot, FieldVector}

//class ArrowIntVector(storage: av.IntVector) extends sdt.IntVector {
//  def apply(i: Int): ValueType = storage.get(i)
//  override def length = storage.getValueCount
//  override def range(start: Int, length: Int): VecT = {
//    val tp = storage.getTransferPair(storage.getAllocator)
//    tp.splitAndTransfer(start, length)
//    new ArrowIntVector(tp.getTo.asInstanceOf[av.IntVector])
//  }
//  override def close(): Unit = storage.close()
//}
//
//class ArrowRealVector(storage: av.Float8Vector) extends sdt.RealVector {
//  def apply(i: Int): ValueType = storage.get(i)
//  def length = storage.getValueCount
//  override def range(start: Int, length: Int): VecT = {
//    val tp = storage.getTransferPair(storage.getAllocator)
//    tp.splitAndTransfer(start, length)
//    new ArrowRealVector(tp.getTo.asInstanceOf[av.Float8Vector])
//  }
//  override def close(): Unit = storage.close()
//}
//
//class ArrowBoolVector(storage: av.BitVector) extends sdt.BoolVector {
//  def apply(i: Int): ValueType = storage.get(i) == 1
//  def length = storage.getValueCount
//  override def range(start: Int, length: Int): VecT = {
//    val tp = storage.getTransferPair(storage.getAllocator)
//    tp.splitAndTransfer(start, length)
//    new ArrowBoolVector(tp.getTo.asInstanceOf[av.BitVector])
//  }
//  override def close(): Unit = storage.close()
//}
//
//class ArrowCharVector(storage: av.VarCharVector) extends sdt.CharVector {
//  def apply(i: Int): ValueType = new String(storage.get(i), StandardCharsets.UTF_8)
//  def length = storage.getValueCount
//  override def range(start: Int, length: Int): VecT = {
//    val tp = storage.getTransferPair(storage.getAllocator)
//    tp.splitAndTransfer(start, length)
//    new ArrowCharVector(tp.getTo.asInstanceOf[av.VarCharVector])
//  }
//  override def close(): Unit = storage.close()
//}

//class ArrowVector[StorageT <: sdt.IStorage[ValueT], ValueT](storage: StorageT) extends sdt.Vector[ValueT] {
//  val tpe = sdt.VectorType.Real
//  override def range(start: Int, length: Int): VecT = {
//    // TODO
//    this
//    //    val tp = storage.getTransferPair(storage.getAllocator)
//    //    tp.splitAndTransfer(start, length)
//    //    new ArrowCharVector(tp.getTo.asInstanceOf[av.VarCharVector])
//  }
//  override def close(): Unit = storage.close()
//}

//class ArrowIncrementalVectorBuilder[VecType <: sdt.TypedVector, ValueT](storage: StorageT) extends
//  sdt.IncrementalVectorBuilder[sdt.Vector[ValueT], ValueT] {
//  override def append(value: ValueType): Unit = {
//    num_elements += 1
//    storage.set_safe(num_elements - 1, value)
//  }
//
//  override def toVector: VecType = {
//    storage.set_value_count(num_elements)
//    new ArrowVector(storage)
//  }
//}