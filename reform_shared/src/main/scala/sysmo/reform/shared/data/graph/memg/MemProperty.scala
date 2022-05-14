package sysmo.reform.shared.data.graph.memg

import sysmo.reform.shared.data.graph.tplight.{Property, Element}

case class MemProperty[V](key: String, value: Option[V], element: Element) extends Property[V]

