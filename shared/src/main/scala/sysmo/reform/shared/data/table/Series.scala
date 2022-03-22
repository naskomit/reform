package sysmo.reform.shared.data.table

trait SeriesBuilder {
  def append(x: Option[Any]): Unit
  def append_value(x: Value[_]): Unit
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
    case Some(v: Long) if field.field_type.tpe == VectorType.Int => vb.asInstanceOf[IVB[Int]] :+ Some(v.toInt)
    case Some(v: Double) if field.field_type.tpe == VectorType.Real => vb.asInstanceOf[IVB[Double]] :+ Some(v)
    case Some(v: Boolean) if field.field_type.tpe == VectorType.Bool => vb.asInstanceOf[IVB[Boolean]] :+ Some(v)
    case Some(v: String) if field.field_type.tpe == VectorType.Char => vb.asInstanceOf[IVB[String]] :+ Some(v)
    case Some(v: java.util.Date) if field.field_type.tpe == VectorType.Real => vb.asInstanceOf[IVB[Double]] :+ Some(v.getTime.toDouble)
    case _ => throw new IllegalArgumentException(f"Cannot add value $x to series of type ${field.field_type.tpe}")
  }

  def append_value(x: Value[_]): Unit = append(x.v)

  override def toSeries: Series = new SeriesImpl(field, vb.toVector)
}

trait Series extends Iterable[Value[_]] {
  val field: Field
  def length: Int
  def get(i: Int): Value[_]
  def is_categorical: Boolean
  def to_categorical(categories: Option[Seq[String]] = None): Series
  override def toString: String
  def vmap(result_field: Field)(f: Value[_] => Value[_]): Series
}

trait SeriesCategorical {
  def categories: Seq[String]

}

class SeriesIterator(s: Series) extends Iterator[Value[_]] {
  var index = 0
  override def hasNext: Boolean = index < s.length
  override def next(): Value[_] = {
    index += 1
    s.get(index - 1)
  }
}

class SeriesImpl(val field: Field, val data: Vector[_, _]) extends Series with SeriesCategorical {
  def length: Int = data.length
  def get(i: Int): Value[_] =  (field.field_type.tpe, field.field_type.ext_class) match {
    case (VectorType.Real, Date) => Value.date(data(i).asInstanceOf[Option[Double]])
    case (VectorType.Real, DateTime) => Value.date(data(i).asInstanceOf[Option[Double]])
    case (VectorType.Real, Same) => Value.real(data(i).asInstanceOf[Option[Double]])
    case (VectorType.Int, Categorical) => CategoricalValue(data(i).asInstanceOf[Option[Int]], field.field_type.categories)
    case (VectorType.Int, Same) => Value.int(data(i).asInstanceOf[Option[Int]])
    case (VectorType.Bool, Same) => Value.bool(data(i).asInstanceOf[Option[Boolean]])
    case (VectorType.Char, Same) => Value.char(data(i).asInstanceOf[Option[String]])
    case (_, _) => throw new IllegalStateException("Illegal combination of storage type and ext_class")
  }

  def vmap(result_field: Field)(f: Value[_] => Value[_]): Series = {
    val builder = data.manager.incremental_series_builder(result_field)
    this.foreach(x => builder :+ f(x).get_opt)
    builder.toSeries
  }

  def is_categorical: Boolean = field.field_type.ext_class == Categorical
  def to_categorical(categories: Option[Seq[String]]): Series = {
    if (is_categorical)
      return this
    val cats = categories match {
      case Some(x) => x
      case None => this.map(x => x.as_char).collect({case Some(x) => x}).toVector.distinct
    }
    val cat_map = cats.zipWithIndex.toMap
    val res_field = field.copy(field_type = field.field_type.copy(tpe = VectorType.Int, ext_class = Categorical, categories = cats))
    this.vmap(res_field)(x => Value.int(x.as_char.flatMap(cat_map.get)))
  }

  def categories: Seq[String] = field.field_type.categories

  override def toString: String = data.toString

  override def iterator: Iterator[Value[_]] = new SeriesIterator(this)
}
