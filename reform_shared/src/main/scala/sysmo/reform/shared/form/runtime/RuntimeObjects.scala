package sysmo.reform.shared.form.runtime

import sysmo.reform.shared.form.{build => FB}
import scala.collection.mutable

sealed trait RuntimeObject extends Product with Serializable {
  type Prototype <: FB.FormElement
  val id: ObjectId
  val prototype: Prototype
  val parent_rel: Option[ParentRelation]
  var runtime : FormRuntime = null
  def parent: Option[RuntimeObject] = parent_rel.map(_.parent)
}

case class ParentRelation(parent: RuntimeObject, relation: FB.FormRelation)

case class AtomicValue(prototype: FB.AtomicField, value: FieldValue[_], id: ObjectId, parent_rel: Option[ParentRelation]) extends RuntimeObject {
  override type Prototype = FB.AtomicField
  type ValueType = prototype.ValueType
}

trait WithNamedChildren[K, V] extends RuntimeObject {
  val children: mutable.Map[K, V] = new mutable.HashMap()
  def child(key: K): Option[V] = {
    children.get(key)
  }
}

trait WithOrderedChildren[V] extends RuntimeObject {
  val children: mutable.ArrayBuffer[V] = new mutable.ArrayBuffer()
}

case class ObjectId(id: Int) extends AnyVal {
  def next: ObjectId = ObjectId(id + 1)
}

object ObjectId {
  val start = ObjectId(0)
}

case class Group(prototype: FB.FieldGroup, id: ObjectId, parent_rel: Option[ParentRelation]) extends RuntimeObject
  with WithNamedChildren[String, ObjectId] {
  override type Prototype = FB.FieldGroup
  def element_iterator: Iterator[(String, RuntimeObject)] = {
    prototype.field_relations.iterator.map {rel =>
      val key = rel.name
      val child = children.get(key).flatMap(runtime.get(_)).get
      (key, child)
    }
  }
}

case class Array(prototype: FB.GroupArray, id: ObjectId, parent_rel: Option[ParentRelation]) extends RuntimeObject
  with WithOrderedChildren[ObjectId] {
  override type Prototype = FB.GroupArray
  def element_iterator: Iterator[Group] = children.map(cid => runtime.get(cid) match {
    case Some(c: Group) => c
    case None => throw new IllegalStateException(s"Array element $cid not found or not a group!")
  }).iterator
}
