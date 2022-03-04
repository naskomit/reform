package sysmo.reform.shared.data.table

trait SeriesBuilder {
  def :+(x: Any): Unit
  def :++(xs: Seq[Any]): Unit = xs.foreach(this :+ _)
  def toSeries: Series
}

class SeriesBuilderImpl(field: Field, vb: IncrementalVectorBuilder[_, _]) extends SeriesBuilder {
  if (!(field.field_type.tpe == vb.tpe)) throw new IllegalArgumentException("The types of field and builder do not match")

  type IVB[A] = IncrementalVectorBuilder[A, VectorStorage[A]]
  override def :+(x: Any): Unit = x match {
    case v: Int if field.field_type.tpe == VectorType.Int => vb.asInstanceOf[IVB[Int]] :+ v
    case v: Double if field.field_type.tpe == VectorType.Real => vb.asInstanceOf[IVB[Double]] :+ v
    case v: Boolean if field.field_type.tpe == VectorType.Bool => vb.asInstanceOf[IVB[Boolean]] :+ v
    case v: String if field.field_type.tpe == VectorType.Char => vb.asInstanceOf[IVB[String]] :+ v
    case _ => throw new IllegalArgumentException(f"Cannot add value $x to series of type ${field.field_type.tpe}")
  }

  override def toSeries: Series = new SeriesImpl(field, vb.toVector)
}

trait Series {
  val field: Field
  def length: Int
  def get(i: Int): Value
  override def toString: String
}

class SeriesImpl(val field: Field, val data: Vector[_, _]) extends Series {
  def length: Int = data.length
  def get(i: Int): Value = Value(data(i))
  override def toString: String = data.toString
}
