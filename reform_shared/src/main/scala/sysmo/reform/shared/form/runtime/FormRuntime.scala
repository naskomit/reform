package sysmo.reform.shared.form.runtime

import sysmo.reform.shared.gremlin.{tplight => TP}
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.util.LabeledValue

import scala.collection.mutable

sealed trait RuntimeObject extends Product with Serializable {
  val id: ObjectId
  val prototype: FB.FormElement
  val parent: Option[RuntimeObject]
  var runtime : FormRuntime = null
}

case class AtomicValue(prototype: FB.AtomicField, value: FieldValue[_], id: ObjectId, parent: Option[RuntimeObject]) extends RuntimeObject

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

case class Group(prototype: FB.FieldGroup, id: ObjectId, parent: Option[RuntimeObject]) extends RuntimeObject
  with WithNamedChildren[String, ObjectId] {
}

case class Array(prototype: FB.GroupArray, id: ObjectId, parent: Option[RuntimeObject]) extends RuntimeObject
  with WithOrderedChildren[ObjectId]

/** The main runtime class */
class FormRuntime(val type_graph: TP.Graph) {
  var current_id: ObjectId = ObjectId.start
  val objects: mutable.Map[ObjectId, RuntimeObject] = mutable.HashMap()

  def get(id: ObjectId): Option[RuntimeObject] = objects.get(id)

  protected def new_id: ObjectId = {
    current_id = current_id.next
    current_id
  }

  def create_object[T <: RuntimeObject](create_fn: ObjectId => T): T = {
    val obj = create_fn(new_id)
    objects.put(obj.id, obj)
    obj
  }
}

object FormRuntime {
  def apply(type_graph: TP.Graph): FormRuntime = new FormRuntime(type_graph)
}

object instantiation {
  trait InstanceBuilder {
    def build(prototype_bound: Option[FB.FormElement], parent: Option[RuntimeObject], rt: FormRuntime): RuntimeObject
  }

  case class AtomicBuilder(v: Any) extends InstanceBuilder {
    override def build(prototype_bound: Option[FB.FormElement], parent: Option[RuntimeObject], rt: FormRuntime): RuntimeObject = {
      prototype_bound match {
        case Some(p: FB.AtomicField) =>
          rt.create_object(id => AtomicValue(p, SomeValue(LabeledValue(v)), id, parent))
        case _ => throw new IllegalArgumentException("Prototype for AtomicValue should be of type AtomicField")
      }
    }
  }

  case class ArrayBuilder(children: Seq[InstanceBuilder]) extends InstanceBuilder {
    override def build(prototype_bound: Option[FB.FormElement], parent: Option[RuntimeObject], rt: FormRuntime): RuntimeObject = {
      prototype_bound match {
        case Some(p: FB.GroupArray) =>
          val array = rt.create_object(id => Array(p, id, parent))
          children.foreach{bld =>
            val child_instance = bld.build(Some(p.prototype), Some(array), rt)
            array.children.append(child_instance.id)
          }
          array

        case _ => throw new IllegalArgumentException("Prototype for Array should be of type GroupArray")
      }
    }
  }

  case class GroupBuilder(prototype: FB.FieldGroup, children: Seq[(String, InstanceBuilder)]) extends InstanceBuilder {
    override def build(prototype_bound: Option[FB.FormElement], parent: Option[RuntimeObject], rt: FormRuntime): RuntimeObject = {
      prototype_bound match {
        case Some(pb: FB.FieldGroup) => {
          if (prototype != pb) {
            throw new IllegalArgumentException(s"Group prototype ${prototype.symbol} does not match prototype bound ${pb.symbol}")
          }
        }
        case Some(pb: FB.GroupUnion) => {
          if (!pb.supertype_of(prototype)) {
            throw new IllegalArgumentException(s"Type ${prototype.symbol} is not subtype of ${pb.symbol}")
          }
        }
        case Some(pb) => throw new IllegalArgumentException(s"Expected object with bound ${pb}, but got FieldGroup(${prototype.symbol})")
        case None =>
      }

      val group = rt.create_object(id => Group(prototype, id, parent))
      children.foreach {
        case (name, builder) => {
          val child_instance = prototype.field(name) match {
            case Some(p) => {
              builder.build(Some(p), Some(group), rt)
            }
            case None => throw new IllegalArgumentException(s"No such field $name in group ${prototype.symbol}")
          }
          group.children.addOne((name, child_instance.id))
        }
      }
      group
    }
  }
  // Atomic values converters
  implicit def str2ab(x: String): AtomicBuilder = AtomicBuilder(x)

  implicit def seq2builder(x: Seq[InstanceBuilder]): ArrayBuilder = ArrayBuilder(x)

  implicit class FormGroupApply(g: FB.FieldGroup) {
    def apply(children: (String, InstanceBuilder)*): GroupBuilder = GroupBuilder(g, children)
  }


  implicit class FormRuntimeInst(rt: FormRuntime) {
    def instantiate(x: InstanceBuilder): RuntimeObject = x.build(None, None, rt)
  }
}