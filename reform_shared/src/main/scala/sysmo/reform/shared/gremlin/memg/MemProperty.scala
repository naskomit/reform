package sysmo.reform.shared.gremlin.memg

import sysmo.reform.shared.gremlin.tplight.Element
import sysmo.reform.shared.gremlin.tplight.Property

case class MemProperty[V](key: String, value: Option[V], element: Element) extends Property[V] {
  def remove(): Unit = {
    element.asInstanceOf[MemElement]._properties.remove(key)
  }
}

