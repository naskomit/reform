package sysmo.reform.shared.form.runtime

import sysmo.reform.shared.expr.Context
import sysmo.reform.shared.form.{build => FB}

import scala.collection.mutable
import sysmo.reform.shared.{expr => E}

sealed trait RuntimeObject extends Product with Serializable {
  type Prototype <: FB.FormElement
  val id: ObjectId
  val prototype: Prototype
  val parent_rel: Option[ParentRelation]
  var runtime : FormRuntime = null
  def parent: Option[RuntimeObject] = parent_rel.map(_.parent)
  def as_context: E.Context[_] = ???
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

case class GroupElement(relation: FB.HasElement, target: RuntimeObject)

case class Group(prototype: FB.FieldGroup, id: ObjectId, parent_rel: Option[ParentRelation]) extends RuntimeObject
  with WithNamedChildren[String, ObjectId] {
  override type Prototype = FB.FieldGroup
  def element_iterator: Iterator[GroupElement] = {
    prototype.field_relations.iterator.map {rel =>
      val key = rel.name
      val child = children.get(key).flatMap(runtime.get(_)).get
      GroupElement(rel, child)
    }
  }

  override def as_context: Context[_] = new Context[FieldValue[_]] {
    type FV = FieldValue[_]
    override def removed(key: String): Map[String, FV] = ???
    override def updated[V1 >: FV](key: String, value: V1): Map[String, V1] = ???
    override def get(key: String): Option[FV] = {
      val child: Option[RuntimeObject] = children.get(key).flatMap(runtime.get)
      child match {
        case Some(AtomicValue(_, value, _, _)) => Some(value)
        case _ => None
      }
    }
    override def iterator: Iterator[(String, FV)] = ???
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

case class Reference(prototype: FB.Reference, id: ObjectId, parent_rel: Option[ParentRelation], ref_id: FieldValue[ObjectId]) extends RuntimeObject {
  override type Prototype = FB.Reference
  def element_iterator: Iterator[RuntimeObject] = ref_id match {
    case NoValue => Iterator.empty
    case AllValues => ???
    case SomeValue(v) => runtime.get(v.value) match {
      case Some(obj) => Seq(obj).iterator
      case None => Seq().iterator
    }
    case MultiValue(v) => {
      v.map(r => runtime.get(r.value)).collect {
        case Some(obj) => obj
      }.iterator
    }
  }
}

//case class ReferenceArray(prototype: FB.Reference, id: ObjectId, parent_rel: Option[ParentRelation], ref_ids: Seq[ObjectId]) extends RuntimeObject
//  with WithOrderedChildren[ObjectId] {
//  override type Prototype = FB.Reference
//}