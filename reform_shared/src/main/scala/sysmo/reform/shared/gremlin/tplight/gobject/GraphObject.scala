package sysmo.reform.shared.gremlin.tplight.gobject

import sysmo.reform.shared.gremlin.tplight._

trait GraphObject {
  def graph: Graph
  def g: GraphTraversalSource = graph.traversal()
}

case class SetPropertyValue[T](p: Property[T], v: Option[T])

case class Property[T](name: String, default: Option[T] = None) {
  def set(v: T): SetPropertyValue[T] = SetPropertyValue(this, Some(v))
  def :=(v: T): SetPropertyValue[T] = SetPropertyValue(this, Some(v))
  def clear: SetPropertyValue[T] = SetPropertyValue(this, None)
}

case class PropertiesUpdate(key_values: Seq[SetPropertyValue[_]])

trait PropertyDef

trait ElementDef {
  val label: String
  type Props <: PropertyDef
  val props: Props

}

trait ElementObj extends GraphObject {
  type ED <: ElementDef
  val ed: ED
  type Props = ed.Props
  def element: Element
  def get[T](f: Props => Property[T]): Option[T] = {
    val p = f(ed.props)
    element.value[T](p.name) match {
      case Some(x) => Some(x)
      case None => p.default
    }
  }

  def update(f_list: (Props => SetPropertyValue[_])*): Unit = {
    val updates = f_list.map(f => f(ed.props))
    update(PropertiesUpdate(updates))
  }

  def update(u: PropertiesUpdate): Unit = {
    u.key_values.foreach{ case SetPropertyValue(p, v) =>
      element.property(p.name, v)
    }
  }
}

trait VertexDef extends ElementDef {

}

trait VertexObj extends ElementObj {
  override type ED <: VertexDef
  val vertex: Vertex
  override def element: Element = vertex
  def graph: Graph = vertex.graph
  def g_this: GraphTraversalBuilder[Vertex, Vertex] = g.V(vertex.id)
}

trait EdgeDef extends ElementDef {

}

trait EdgeObj extends ElementObj {
  override type ED <: EdgeDef
  val edge: Edge
  override def element: Element = edge
  def graph: Graph = edge.graph
}
