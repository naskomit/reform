package sysmo.reform.shared.data.table

trait SeriesBuilder {
  def append(x: Option[Any]): Unit
  def :+(x: Option[Any]): Unit = append(x)
  def :++(xs: Seq[Option[Any]]): Unit = xs.foreach(append)
  def toSeries: Series
}

class SeriesBuilderImpl(field: Field, vb: IncrementalVectorBuilder[_, _]) extends SeriesBuilder {
  if (!(field.field_type.tpe == vb.tpe)) throw new IllegalArgumentException("The types of field and builder do not match")

  type IVB[A] = IncrementalVectorBuilder[A, VectorStorage[A]]
  override def append(x: Option[Any]): Unit = x match {
    case None => vb.append(None)
    case Some(v: Int) if field.field_type.tpe == VectorType.Int => vb.asInstanceOf[IVB[Int]] :+ Some(v)
    case Some(v: Double) if field.field_type.tpe == VectorType.Real => vb.asInstanceOf[IVB[Double]] :+ Some(v)
    case Some(v: Boolean) if field.field_type.tpe == VectorType.Bool => vb.asInstanceOf[IVB[Boolean]] :+ Some(v)
    case Some(v: String) if field.field_type.tpe == VectorType.Char => vb.asInstanceOf[IVB[String]] :+ Some(v)
    case _ => throw new IllegalArgumentException(f"Cannot add value $x to series of type ${field.field_type.tpe}")
  }

  override def toSeries: Series = new SeriesImpl(field, vb.toVector)
}

trait Series extends Iterable[Value] {
  val field: Field
  def length: Int
  def get(i: Int): Value
  override def toString: String
}

class SeriesIterator(s: Series) extends Iterator[Value] {
  var index = 0
  override def hasNext: Boolean = index < s.length
  override def next(): Value = {
    index += 1
    s.get(index - 1)
  }
}

class SeriesImpl(val field: Field, val data: Vector[_, _]) extends Series {
  def length: Int = data.length
  def get(i: Int): Value = new Value(data(i), field.field_type.tpe)
  override def toString: String = data.toString

  override def iterator: Iterator[Value] = new SeriesIterator(this)
}
