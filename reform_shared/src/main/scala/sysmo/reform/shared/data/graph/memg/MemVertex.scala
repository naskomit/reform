package sysmo.reform.shared.data.graph.memg

import sysmo.reform.shared.data.graph.tplight.{Edge, Graph, PropId, Property, Vertex}

import scala.collection.mutable

case class MemVertex[IdType](graph: MemGraph[IdType], id: IdType, label: String) extends MemElement[IdType] with Vertex[IdType] {
  val outEdges: mutable.HashMap[String, Set[Edge[IdType]]] = mutable.HashMap()
  val inEdges: mutable.HashMap[String, Set[Edge[IdType]]] = mutable.HashMap()

  override def add_edge(label: String, to: Vertex[IdType], key_values: Tuple2[PropId, Any]*): Edge[IdType] = {
    val edge = to match {
      case mto: MemVertex[IdType] => graph.add_element(label, key_values)((_, eid, elabel) => MemEdge[IdType](graph, eid, elabel, this, mto))
    }
    graph.insert_edge(edge)

    edge

  }

  override def toString: String = s"v[$id:$label]"

}
