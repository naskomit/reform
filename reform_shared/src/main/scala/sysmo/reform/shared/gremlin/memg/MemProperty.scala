package sysmo.reform.shared.gremlin.memg

import sysmo.reform.shared.gremlin.tplight.{Element, Property}

case class MemProperty[V](key: String, value: Option[V], element: Element) extends Property[V]

