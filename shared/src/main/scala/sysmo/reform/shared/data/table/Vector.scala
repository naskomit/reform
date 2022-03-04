package sysmo.reform.shared.data.table

abstract class VectorStorage[ValueT](val manager: TableManager) {
  def set_safe(index: Int, v: ValueT)
  def get(index: Int): ValueT
  def set_value_count(i: Int): Unit
  def get_value_count: Int
  def range_view(start: Int, length: Int): VectorStorage[ValueT]
  def close(): Unit
  def name: String
}

class Vector[V, +Storage <: VectorStorage[V]](storage: Storage)(implicit tci: VectorTypeclass[V])
    extends Iterable[V] with AutoCloseable {
  type ValueType = V
//  type VecT = Vector[V, Storage]
  def name: String = storage.name
  def apply(i: Int): ValueType = storage.get(i)
  def length: Int = storage.get_value_count
  def iterator = new VectorIterator[V](this)
  def range(start: Int, length: Int): Vector[V, Storage] =
    // Not sure why this conversion is necessary, but doesn't work otherwise
    new Vector[V, Storage](storage.range_view(start, length).asInstanceOf[Storage])

  def map2[B](f: V => B)(implicit tci_out: VectorTypeclass[B]): Vector[B, tci_out.Storage] = {
    val builder = new IncrementalVectorBuilder[B, tci_out.Storage](storage.manager.create_storage("")(tci_out))
    this.foreach(el => builder :+ f(el))
    builder.toVector
  }

  def tpe: VectorType.Value = tci.tpe
  def show: String = f"${tpe}(" + this.mkString(", ") + ")"
  override def toString: String = show

  override def close(): Unit = {
    storage.close()
    storage.manager.on_free_storage(storage)
  }
}

class VectorIterator[V](vec: Vector[V, _]) extends Iterator[V] {
  var index = 0
  override def hasNext: Boolean = index < vec.length
  override def next(): V = {
    index += 1
    vec(index - 1)
  }
}

// TODO Not safe (not auto close-able)
abstract class VectorBuilder[V, Storage <: VectorStorage[V]](storage: Storage)(implicit tci: VectorTypeclass[V]) {
  def tpe: VectorType.Value = tci.tpe
  def toVector: Vector[V, Storage]
}

//trait RandomAccessVectorBuilder[VecT <: TypedVector, V] extends VectorBuilder[VecT, V] {
//  def set_safe(i: Int, value: ValueType)
//}

class IncrementalVectorBuilder[V, Storage <: VectorStorage[V]]
  (storage: Storage)(implicit tci: VectorTypeclass[V])
    extends VectorBuilder[V, Storage](storage) {
  var num_elements: Int = 0
  def append(value: V): Unit = {
    num_elements += 1
    storage.set_safe(num_elements - 1, value)
  }

  override def toVector: Vector[V, Storage] = {
    storage.set_value_count(num_elements)
    new Vector[V, Storage](storage)
  }

  def :+(value: V) = append(value)
  def ++= (value: Seq[V]) = value.foreach(x => append(x))
}
