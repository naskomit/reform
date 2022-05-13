package sysmo.reform.shared.data.graph.memg

import sysmo.reform.shared.data.graph.tplight.{Element, Property}

import scala.collection.mutable

trait MemElement[IdType] extends Element[IdType] {
  protected val properties: mutable.HashMap[String, Property[_]] = mutable.HashMap()

  /** Returns set of property keys */
  override def keys: Set[String] = properties.keys.toSet

  /** Returns property */
  override def property[V](key: String): Property[V] =
    properties.getOrElse(key, Property.empty[V]).asInstanceOf[Property[V]]

  /** Set or update property */
  override def property[V](key: String, value: V): Property[V] = {
    val prop = new MemProperty[V](key, Some(value), this)
    properties += (key -> prop)
    prop
  }

  /** Returns property value */
  override def value[V](key: String): Option[V] =
    properties.get(key).flatMap(x => x.value).map(_.asInstanceOf[V])
}
