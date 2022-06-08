package sysmo.reform.shared.form

import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.{expr => E}

sealed trait FieldValue[+V]
case object NoValue extends FieldValue[Nothing]
case object AllValues extends FieldValue[Nothing]
case class SomeValue[+V](v: LabeledValue[V]) extends FieldValue[V] {
  override def equals(obj: Any): Boolean = obj match {
    case SomeValue(v_other) => v.value == v_other.value
    case _ => false
  }

  override def hashCode(): Int = v.hashCode()
}
case class MultiValue[+V](v: Seq[LabeledValue[V]]) extends FieldValue[V]
case class LocalFieldIndex(ids: Seq[FieldId]) extends FieldValue[FieldId]

object FieldValue {
//  def apply(x: String): FieldValue[String] = SomeValue(LabeledValue(x))
//  def apply(x: String, label: String): FieldValue[String] = SomeValue(LabeledValue(x, Some(label)))
//  def apply(x: Int): FieldValue[Int] = SomeValue(LabeledValue(x))
//  def apply(x: Int, label: String): FieldValue[Int] = SomeValue(LabeledValue(x, Some(label)))
//  def apply(x: Double): FieldValue[Double] = SomeValue(LabeledValue(x))
//  def apply(x: Double, label: String): FieldValue[Double] = SomeValue(LabeledValue(x, Some(label)))
//  def apply(x: Boolean): FieldValue[Boolean] = SomeValue(LabeledValue(x))
//  def apply(x: Boolean, label: String): FieldValue[Boolean] = SomeValue(LabeledValue(x, Some(label)))
//  def apply(x: Option[_]): FieldValue[_] = x match {
//    case None => NoValue
//    case Some(v: LabeledValue[_]) => SomeValue(v)
//    case Some(v) => SomeValue(LabeledValue(v))
//  }
//  def apply(x: Seq[_]): FieldValue[_] = MultiValue(x.map {
//    case v: LabeledValue[_] => v
//    case v => LabeledValue(v)
//  })
  object implicits {
    implicit def str2expr(x: String): E.Constant = E.Constant(SomeValue(LabeledValue(x)))
    implicit def float2expr(x: Double): E.Constant = E.Constant(SomeValue(LabeledValue(x)))
    implicit def int2expr(x: Int): E.Constant = E.Constant(SomeValue(LabeledValue(x)))
    implicit def bool2expr(x: Boolean): E.Constant = E.Constant(SomeValue(LabeledValue(x)))
  }
//  def apply[V](v: Any): FieldValue[V] = v match {
//    case None => NoValue
//    case Some(x) => SomeValue(LabeledValue[V](x))
//  }
}

//trait FieldLV
//
//trait FVConverter[T] {
//  val v: T
//  def fv: SomeValue[T] = SomeValue(LabeledValue(v))
//}

class ValueMap(data: Map[ElementPath, FieldValue[_]]) {
//  def apply(path: ElementPath): FieldValue[_] = data(path)
  def get(path: ElementPath): FieldValue[_] = data.getOrElse(path, NoValue)
  def update(path: ElementPath, value: FieldValue[_]): ValueMap = {
    new ValueMap(data + (path -> value))
  }
  def remove(path: ElementPath) = {
    new ValueMap(data.removed(path))
  }
  def toMap: Map[ElementPath, FieldValue[_]] = data
  override def toString: String = s"ValueMap(${data.map {
    case(k, v) => {
      val v_str = v match {
        case NoValue => ""
        case AllValues => "ALL"
        case SomeValue(x) => x.make_label
        case MultiValue(x) => x.map(_.make_label).toString()
        case LocalFieldIndex(ids) => ids.map("#" + _.toString)
      }
      s"${k.toString} -> $v_str"
    } .mkString("")
  }})"
}
object ValueMap {
  def apply(items: Tuple2[ElementPath, FieldValue[_]]*): ValueMap = new ValueMap(items.toMap)

  class Builder(path: ElementPath) {
    protected var data: Map[ElementPath, FieldValue[_]] = Map()

    def merge(other: Builder): this.type = {
      for ((k_sub, v) <- other.data) {
        data = data + (k_sub -> v)
      }
      this
    }

    def value(k: String, v: FieldValue[_]): this.type = {
      data = data + ((path / k) -> v)
      this
    }

    def value(k: String, v: Int): this.type = value(k, SomeValue(LabeledValue(v)))
    def value(k: String, v: Double): this.type = value(k, SomeValue(LabeledValue(v)))
    def value(k: String, v: Boolean): this.type = value(k, SomeValue(LabeledValue(v)))
    def value(k: String, v: String): this.type = value(k, SomeValue(LabeledValue(v)))
    def value(k: String, v: Seq[_]): this.type = value(k, MultiValue(v.map(LabeledValue(_))))

    def record(k: String, sub_fn: Builder => Builder): this.type = {
      val sub = sub_fn(new Builder(path / k))
      merge(sub)
    }

    def array(k: String, sub_fns: Builder => Builder*): this.type = {
      val index_path = path / k
      val item_uids = sub_fns.map { sub_fn =>
        val uid = ArrayFieldId.random()
        val item_builder = sub_fn(new Builder(index_path / uid))
        merge(item_builder)
        uid
      }
      data = data + (index_path -> LocalFieldIndex(item_uids))
      this
    }

    def build: ValueMap = new ValueMap(data)
  }

  def builder: Builder = new Builder(ElementPath.Empty)
}

// TODO Implement the other methods
class HandlerContext(base: FormElement, data: ValueMap) extends E.Context[FieldValue[_]] {
  type FV = FieldValue[_]
  override def removed(key: String): Map[String, FV] = ???
  override def updated[V1 >: FV](key: String, value: V1): Map[String, V1] = ???
  override def get(key: String): Option[FV] = data.toMap.get(base.path / key)
  override def iterator: Iterator[(String, FV)] = ???
}
