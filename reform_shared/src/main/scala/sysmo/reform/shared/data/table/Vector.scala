package sysmo.reform.shared.data.table

abstract class VectorStorage[ValueT](val manager: TableManager) {
  def set_safe(index: Int, v: ValueT)
  def get(index: Int): Option[ValueT]
  def set_value_count(i: Int): Unit
  def get_value_count: Int
  def range_view(start: Int, length: Int): VectorStorage[ValueT]
  /** Finalize mutations and convert to immutable */
  def seal: VectorStorage[ValueT]
  def close(): Unit
  def name: String
}

class Vector[V, +Storage <: VectorStorage[V]](storage: Storage)(implicit tci: VectorTypeclass[V])
    extends Iterable[Option[V]] with AutoCloseable {
  type ValueType = V
//  type VecT = Vector[V, Storage]
  def name: String = storage.name
  def apply(i: Int): Option[ValueType] = storage.get(i)
  def length: Int = storage.get_value_count
  def manager: TableManager = storage.manager
  def iterator = new VectorIterator[V](this)
  def range(start: Int, length: Int): Vector[V, Storage] =
    // Not sure why this conversion is necessary, but doesn't work otherwise
    new Vector[V, Storage](storage.range_view(start, length).asInstanceOf[Storage])

  def vmap[B](f: V => B)(implicit tci_out: VectorTypeclass[B]): Vector[B, VectorStorage[B]] = {
    val builder = new IncrementalVectorBuilder[B, tci_out.MutableStorage](storage.manager.create_mutable_storage("")(tci_out))
    this.foreach {
      case Some(v) => builder :+ Some(f(v))
      case None => builder :+ None
    }
    builder.toVector
  }

  def tpe: VectorType.Value = tci.tpe
  def show: String = f"${tpe}(" + this.map {
    case Some(x) => x.toString
    case None => "N/A"
  }.mkString(", ") + ")"
  override def toString: String = show

  override def close(): Unit = {
    storage.close()
    storage.manager.on_free_storage(storage)
  }
}

class VectorIterator[V](vec: Vector[V, _]) extends Iterator[Option[V]] {
  var index = 0
  override def hasNext: Boolean = index < vec.length
  override def next(): Option[V] = {
    index += 1
    vec(index - 1)
  }
}

// TODO Not safe (not auto close-able)
// (storage: Storage)(implicit tci: VectorTypeclass[V])
trait VectorBuilder[V, Storage <: VectorStorage[V]] {
  val storage: Storage
  def tpe: VectorType.Value
  def toVector: Vector[V, VectorStorage[V]]
}

//trait RandomAccessVectorBuilder[VecT <: TypedVector, V] extends VectorBuilder[VecT, V] {
//  def set_safe(i: Int, value: ValueType)
//}

class IncrementalVectorBuilder[V, Storage <: VectorStorage[V]]
  (val storage: Storage)(implicit tci: VectorTypeclass[V])
    extends VectorBuilder[V, Storage] {
  var num_elements: Int = 0
  def tpe: VectorType.Value = tci.tpe
  def append(value: Option[V]): Unit = {
    num_elements += 1
    value match {
      case Some(v) => storage.set_safe(num_elements - 1, v)
      case None =>
    }

  }

  override def toVector: Vector[V, VectorStorage[V]] = {
    storage.set_value_count(num_elements)
    new Vector[V, VectorStorage[V]](storage.seal)
  }

  def :+(value: Option[V]): Unit = append(value)
  def ++= (value: Seq[Option[V]]): Unit = value.foreach(append)
}
