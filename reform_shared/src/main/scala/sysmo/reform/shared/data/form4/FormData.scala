package sysmo.reform.shared.data.form4

import sysmo.reform.shared.util.LabeledValue


sealed trait FieldValue[+V]
case object NoValue extends FieldValue[Nothing]
case object AllValues extends FieldValue[Nothing]
case class SomeValue[+V](v: LabeledValue[V]) extends FieldValue[V]
case class MultiValue[+V](v: Seq[LabeledValue[V]]) extends FieldValue[V]

trait FieldLV

trait FVConverter[T] {
  val v: T
  def fv: SomeValue[T] = SomeValue(LabeledValue(v))
}

class ValueMap(data: Map[ElementPath, FieldValue[_]]) {
  def apply(path: ElementPath): FieldValue[_] = data(path)
  def update(path: ElementPath, value: FieldValue[_]): ValueMap = {
    new ValueMap(data + (path -> value))
  }
  def toMap: Map[ElementPath, FieldValue[_]] = data
  override def toString: String = s"ValueMap(${data.map {
    case(k, v) => {
      val v_str = v match {
        case NoValue => ""
        case AllValues => "ALL"
        case SomeValue(x) => x.make_label
        case MultiValue(x) => x.map(_.make_label).toString()
      }
      s"${k.toString} -> $v_str"
    } .mkString("")
  }})"
}
object ValueMap {
  def apply(items: Tuple2[ElementPath, FieldValue[_]]*): ValueMap = new ValueMap(items.toMap)

  class Builder(path: ElementPath) {
    protected var data: Map[ElementPath, FieldValue[_]] = Map()

    def value(k: String, v: FieldValue[_]): this.type = {
      data = data + ((path / k) -> v)
      this
    }

    def record(k: String, sub_fn: Builder => Builder): this.type = {
      val sub = sub_fn(new Builder(path / k))
      for ((k_sub, v) <- sub.data) {
        data = data + (k_sub -> v)
      }
      this
    }

    def value(k: String, v: Int): this.type = value(k, SomeValue(LabeledValue(v)))
    def value(k: String, v: Double): this.type = value(k, SomeValue(LabeledValue(v)))
    def value(k: String, v: Boolean): this.type = value(k, SomeValue(LabeledValue(v)))
    def value(k: String, v: String): this.type = value(k, SomeValue(LabeledValue(v)))

    def build: ValueMap = new ValueMap(data)
  }

  def builder: Builder = new Builder(ElementPath.Empty)
}

