package sysmo.reform.data.table.arrow

import org.apache.arrow.{vector => av}
import sysmo.reform.shared.data.{table => sdt}

object ArrowVector {
  type RootAllocator = org.apache.arrow.memory.RootAllocator

  abstract class ArrowStorage[ValueType](manager: ArrowTableManager, backend: av.ValueVector) extends sdt.VectorStorage[ValueType](manager) {
    override def set_value_count(i: Int): Unit = backend.setValueCount(i)
    override def get_value_count: Int = backend.getValueCount
//    override def range_view(start: Int, length: Int): ArrowStorage[ValueType] = {
//      val tp = backend.getTransferPair(backend.getAllocator)
//      tp.splitAndTransfer(start, length)
//      new ArrowStorage(manager, tp.getTo.asInstanceOf[backend.type])
//    }
    override def name: String = backend.getName
    override def close(): Unit = backend.close()
  }


//  def incremental_series_builder(manager: sdt.TableManager, field: sdt.Field): sdt.SeriesBuilder = {
//    import sysmo.reform.data.table.ArrowVector._
//    val vec_builder = field.field_type.tpe match {
//      case sdt.VectorType.Real => manager.incremental_vector_builder[Double](field.name)
//      case sdt.VectorType.Int => manager.incremental_vector_builder[Int](field.name)
//      case sdt.VectorType.Bool => manager.incremental_vector_builder[Boolean](field.name)
//      case sdt.VectorType.Char => manager.incremental_vector_builder[String](field.name)
//    }
//    new sdt.SeriesBuilderImpl(field, vec_builder)
//  }

  implicit object ev_real extends sdt.VectorTypeclass[Double] {
    type StorageBackend = av.Float8Vector
    val tpe = sdt.VectorType.Real
    class ImmutableStorage(manager: ArrowTableManager, backend: av.Float8Vector)
        extends  ArrowStorage[ValueType](manager, backend) {
      override def set_safe(index: Int, v: ValueType): Unit = backend.setSafe(index, v)
      override def get(index: Int): Option[ValueType] = {
        val holder = new av.holders.NullableFloat8Holder
        backend.get(index, holder)
        if (holder.isSet == 0)
          None
        else
          Some(holder.value)
      }
      override def range_view(start: Int, length: Int): ImmutableStorage = {
        val tp = backend.getTransferPair(backend.getAllocator)
        tp.splitAndTransfer(start, length)
        new ImmutableStorage(manager, tp.getTo.asInstanceOf[StorageBackend])
      }

      override def seal: sdt.VectorStorage[ValueType] = this
    }
    override type MutableStorage = ImmutableStorage
    override def create_mutable_storage(manager: sdt.TableManager, name: String): MutableStorage = {
      val mngr = manager.asInstanceOf[ArrowTableManager]
      new ImmutableStorage(mngr, new StorageBackend(name, mngr.allocator))
    }
  }

  implicit object ev_int extends sdt.VectorTypeclass[Int] {
    type StorageBackend = av.IntVector
    val tpe = sdt.VectorType.Int
    class ImmutableStorage(manager: ArrowTableManager, backend: StorageBackend)
        extends ArrowStorage[ValueType](manager, backend) {
      override def set_safe(index: Int, v: ValueType): Unit = backend.setSafe(index, v)
      override def get(index: Int): Option[ValueType] = {
        val holder = new av.holders.NullableIntHolder
        backend.get(index, holder)
        if (holder.isSet == 0)
          None
        else
          Some(holder.value)
      }
      override def range_view(start: Int, length: Int): ImmutableStorage = {
        val tp = backend.getTransferPair(backend.getAllocator)
        tp.splitAndTransfer(start, length)
        new ImmutableStorage(manager, tp.getTo.asInstanceOf[StorageBackend])
      }
      override def seal: sdt.VectorStorage[ValueType] = this
    }
    override type MutableStorage = ImmutableStorage
    override def create_mutable_storage(manager: sdt.TableManager, name: String): MutableStorage = {
      val mngr = manager.asInstanceOf[ArrowTableManager]
      new ImmutableStorage(mngr, new StorageBackend(name, mngr.allocator))
    }
  }

  implicit object ev_bool extends sdt.VectorTypeclass[Boolean] {
    type StorageBackend = av.BitVector
    val tpe = sdt.VectorType.Bool
    class ImmutableStorage(manager: ArrowTableManager, backend: StorageBackend)
      extends  ArrowStorage[ValueType](manager, backend) {
      override def set_safe(index: Int, v: ValueType): Unit = backend.setSafe(index, if (v) 1 else 0)
      override def get(index: Int): Option[ValueType] = {
        val holder = new av.holders.NullableBitHolder
        backend.get(index, holder)
        if (holder.isSet == 0)
          None
        else
          Some(holder.value == 1)
      }
      override def range_view(start: Int, length: Int): ImmutableStorage = {
        val tp = backend.getTransferPair(backend.getAllocator)
        tp.splitAndTransfer(start, length)
        new ImmutableStorage(manager, tp.getTo.asInstanceOf[StorageBackend])
      }
      override def seal: sdt.VectorStorage[ValueType] = this
    }
    override type MutableStorage = ImmutableStorage
    override def create_mutable_storage(manager: sdt.TableManager, name: String): MutableStorage = {
      val mngr = manager.asInstanceOf[ArrowTableManager]
      new ImmutableStorage(mngr, new StorageBackend(name, mngr.allocator))
    }
  }

  implicit object ev_char extends sdt.VectorTypeclass[String] {
    import java.nio.charset.StandardCharsets
    type StorageBackend = av.VarCharVector
    val tpe = sdt.VectorType.Char
    class ImmutableStorage(manager: ArrowTableManager, backend: StorageBackend)
      extends  ArrowStorage[ValueType](manager, backend) {
      override def set_safe(index: Int, v: ValueType): Unit = backend.setSafe(index, v.getBytes(StandardCharsets.UTF_8))
      override def get(index: Int): Option[ValueType] = {
        //: ValueType = new String(backend.get(index), StandardCharsets.UTF_8)
        val holder = new av.holders.NullableVarCharHolder
        backend.get(index, holder)
        if (backend.isSet(index) == 0)
          None
        else
          Some(new String(backend.get(index), StandardCharsets.UTF_8))
      }
      override def range_view(start: Int, length: Int): ImmutableStorage = {
        val tp = backend.getTransferPair(backend.getAllocator)
        tp.splitAndTransfer(start, length)
        new ImmutableStorage(manager, tp.getTo.asInstanceOf[StorageBackend])
      }
      override def seal: sdt.VectorStorage[ValueType] = this

    }
    override type MutableStorage = ImmutableStorage
    override def create_mutable_storage(manager: sdt.TableManager, name: String): MutableStorage = {
      val mngr = manager.asInstanceOf[ArrowTableManager]
      new ImmutableStorage(mngr, new StorageBackend(name, mngr.allocator))
    }
  }
}
