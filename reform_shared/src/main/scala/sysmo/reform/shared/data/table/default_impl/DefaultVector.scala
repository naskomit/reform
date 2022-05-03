package sysmo.reform.shared.data.table.default_impl

import scala.collection.{mutable => mut}
import sysmo.reform.shared.data.{table => sdt}

object DefaultVector {
  type RootAllocator = Null

  class MutableStorageImpl[ValueType](manager: DefaultTableManager, _name: String) extends sdt.VectorStorage[ValueType](manager) {
    val backend = mut.ArrayBuffer[Option[ValueType]]()
    override def set_value_count(i: Int): Unit = {
      if (i > backend.size) {
        for (n <- backend.size until i) {
          backend.append(None)
        }
      } else if (i < backend.size) {
        backend.remove(i, backend.size - i)
      }
    }
    override def get_value_count: Int = backend.size
    override def set_safe(index: Int, v: ValueType): Unit = {
      if (index >= backend.size) {
        for (n <- backend.size until index) {
          backend.append(None)
        }
        backend.append(Some(v))
      } else {
        backend(index) = Some(v)
      }
    }
    override def get(index: Int): Option[ValueType] = backend(index)
    override def range_view(start: Int, length: Int): MutableStorageImpl[ValueType] = throw new IllegalStateException("Range only works for immutable vectors!")
    override def name: String = _name
    override def seal: sdt.VectorStorage[ValueType] = {
      new ImmutableStorageImpl[ValueType](manager, _name, backend.toVector)
    }
    override def close(): Unit = {}
  }

  class ImmutableStorageImpl[ValueType](manager: DefaultTableManager, _name: String, backend: Vector[Option[ValueType]]) extends sdt.VectorStorage[ValueType](manager) {
    override def set_value_count(i: Int): Unit = throw new IllegalStateException("Cannot modify value count when in immutable state")
    override def get_value_count: Int = backend.size
    override def set_safe(index: Int, v: ValueType): Unit = throw new IllegalStateException("Cannot modify values when in immutable state!")
    override def get(index: Int): Option[ValueType] = backend(index)
    override def range_view(start: Int, length: Int): ImmutableStorageImpl[ValueType] = {
      new ImmutableStorageImpl[ValueType](manager, "", backend.slice(start, start + length))
    }
    override def seal: sdt.VectorStorage[ValueType] = this
    override def name: String = _name
    override def close(): Unit = {}
  }

  implicit object ev_real extends sdt.VectorTypeclass[Double] {
    val tpe = sdt.VectorType.Real
    type MutableStorage = MutableStorageImpl[Double]
    type ImmutableStorage = ImmutableStorageImpl[Double]
    override def create_mutable_storage(manager: sdt.TableManager, name: String): MutableStorage = {
      val mngr = manager.asInstanceOf[DefaultTableManager]
      new MutableStorage(mngr, name)
    }
  }

  implicit object ev_int extends sdt.VectorTypeclass[Int] {
    val tpe = sdt.VectorType.Int
    type MutableStorage = MutableStorageImpl[Int]
    type ImmutableStorage = ImmutableStorageImpl[Int]
    override def create_mutable_storage(manager: sdt.TableManager, name: String): MutableStorage = {
      val mngr = manager.asInstanceOf[DefaultTableManager]
      new MutableStorage(mngr, name)
    }
  }

  implicit object ev_bool extends sdt.VectorTypeclass[Boolean] {
    val tpe = sdt.VectorType.Bool
    type MutableStorage = MutableStorageImpl[Boolean]
    type ImmutableStorage = ImmutableStorageImpl[Boolean]
    override def create_mutable_storage(manager: sdt.TableManager, name: String): MutableStorage = {
      val mngr = manager.asInstanceOf[DefaultTableManager]
      new MutableStorage(mngr, name)
    }
  }

  implicit object ev_char extends sdt.VectorTypeclass[String] {
    val tpe = sdt.VectorType.Char
    type MutableStorage = MutableStorageImpl[String]
    type ImmutableStorage = ImmutableStorageImpl[String]
    override def create_mutable_storage(manager: sdt.TableManager, name: String): MutableStorage = {
      val mngr = manager.asInstanceOf[DefaultTableManager]
      new MutableStorage(mngr, name)
    }
  }
}
