package sysmo.reform.shared.form.runtime

import sysmo.reform.shared.field.{FieldType, Value}
import sysmo.reform.shared.form.build.HasElement
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.property.{Property, PropertySource}

import scala.reflect.ClassTag

object PropertyView {
  trait RuntimeObjectAsProperty[T] extends Property[ObjectId, Any] {
    val v: RuntimeObject
    override def id: ObjectId = v.id
  }

  class AtomicValueAsProperty(val v: AtomicValue, val name: String, val descr: String) extends RuntimeObjectAsProperty[Any] {
    override def get_value: Value[_] = ???
    override def get_type: FieldType = ???
    override def update_value(v: Value[Any]): Either[Throwable, Unit] = ???
  }

  class GroupAsProperty(val v: Group, val name: String, val descr: String) extends RuntimeObjectAsProperty[Any] {
    override def get_value: Value[_] = ???
    override def get_type: FieldType = ???
    override def update_value(v: Value[Any]): Either[Throwable, Unit] = ???
  }

  class ArrayAsProperty(val v: Array, val name: String, val descr: String) extends RuntimeObjectAsProperty[Any] {
    override def get_value: Value[_] = ???
    override def get_type: FieldType = ???
    override def update_value(v: Value[Any]): Either[Throwable, Unit] = ???
  }

  class ReferenceAsProperty(val v: Reference, val name: String, val descr: String) extends RuntimeObjectAsProperty[Any] {
    override def get_value: Value[_] = ???
    override def get_type: FieldType = ???
    override def update_value(v: Value[Any]): Either[Throwable, Unit] = ???
  }

  class GroupAsPropertySource(group: Group) extends PropertySource {
    type K = ObjectId
    override def properties: Seq[Property[ObjectId, _]] = group.element_iterator.map{element =>
      element.target match {
        case e: AtomicValue => new AtomicValueAsProperty(e, element.relation.name, element.relation.descr)
        case e: Group => new GroupAsProperty(e, element.relation.name, element.relation.descr)
        case e: Array => new ArrayAsProperty(e, element.relation.name, element.relation.descr)
        case e: Reference => new ReferenceAsProperty(e, element.relation.name, element.relation.descr)
      }
    }.toSeq
  }

}
