package sysmo.reform.shared.gremlin.memg

import sysmo.reform.shared.gremlin.tplight.Element
import sysmo.reform.shared.gremlin.tplight.Property

import scala.collection.mutable

trait MemElement extends Element {
  protected[memg] val _properties: mutable.HashMap[String, Property[_]] = mutable.HashMap()

  /** Returns set of property keys */
  override def keys: Set[String] = _properties.keys.toSet

  /** Returns property */
  override def property[V](key: String): Property[V] =
    _properties.getOrElse(key, Property.empty[V]).asInstanceOf[Property[V]]

  /** Set or update property */
  override def property[V](key: String, value: V): Property[V] = {
    val prop = new MemProperty[V](key, Some(value), this)
    _properties += (key -> prop)
    prop
  }

  /** Returns property value */
  override def value[V](key: String): Option[V] =
    _properties.get(key).flatMap(x => x.value).map(_.asInstanceOf[V])
}
