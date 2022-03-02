package sysmo.reform.shared.data.table

trait Vector[V] extends Iterable[V] with AutoCloseable {
  type ValueType = V
  type VecT = Vector[V]
  val tpe: VectorType.VectorType
  def apply(i: Int): ValueType
  def length: Int
  def iterator = new VectorIterator[V](this)
  def range(start: Int, length: Int): VecT

  def show: String = f"${tpe}(" + this.mkString(", ") + ")"
  override def toString: String = show
}

class VectorIterator[V](vec: Vector[V]) extends Iterator[V] {
  var index = 0
  override def hasNext: Boolean = index < vec.length
  override def next(): V = {
    index += 1
    vec(index - 1)
  }
}

object VectorType extends Enumeration {
  type VectorType = Value
  val Int, Real, Bool, Char = Value
}

trait IntVector extends Vector[Int] {
  override val tpe: VectorType.VectorType = VectorType.Int
}

trait RealVector extends Vector[Double] {
  override val tpe: VectorType.VectorType = VectorType.Real
}

trait BoolVector extends Vector[Boolean] {
  override val tpe: VectorType.VectorType = VectorType.Bool
}

trait CharVector extends Vector[String] {
  override val tpe: VectorType.VectorType = VectorType.Char
}

// TODO Not safe (not auto close-able)
trait VectorBuilder[VecT <: Vector[_], V] {
  type ValueType = V
  type VecType = VecT
  def toVector: VecType
}

trait RandomAccessVectorBuilder[VecType <: Vector[_], V] extends VectorBuilder[VecType, V] {
  def set_safe(i: Int, value: ValueType)
}

trait IncrementalVectorBuilder[VecType <: Vector[_], V] extends VectorBuilder[VecType, V] {
  var num_elements: Int = 0
  def append(value: ValueType)
  def :+(value: ValueType) = append(value)
  def ++= (value: Seq[ValueType]) = value.foreach(x => append(x))
}
