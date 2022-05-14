package sysmo.reform.shared.data.graph.memg

import sysmo.reform.shared.data.graph.tplight.{Edge, Graph, PropId, Property, Vertex}

import scala.collection.mutable

case class MemVertex(graph: MemGraph, id: Any, label: String) extends MemElement with Vertex {
  val outEdges: mutable.HashMap[String, Set[Edge]] = mutable.HashMap()
  val inEdges: mutable.HashMap[String, Set[Edge]] = mutable.HashMap()

  override def add_edge(label: String, to: Vertex, key_values: Tuple2[PropId, Any]*): Edge = {
    val edge = to match {
      case mto: MemVertex => graph.add_element(label, key_values)((_, eid, elabel) => new MemEdge(graph, eid, elabel, this, mto))
    }
    graph.insert_edge(edge)

    edge

  }

  override def toString: String = s"v[$id:$label]"

}
