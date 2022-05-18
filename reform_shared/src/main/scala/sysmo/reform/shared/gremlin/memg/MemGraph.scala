package sysmo.reform.shared.gremlin.memg

import sysmo.reform.shared.gremlin.tplight
import sysmo.reform.shared.gremlin.tplight.{Edge, Element, Graph, PropId, Vertex}

import scala.collection.mutable


trait ElementHelper {
  def new_id(): Any
}

class MemGraph(element_helper: ElementHelper) extends Graph {
  protected val vertex_map: mutable.HashMap[Any, Vertex] = mutable.HashMap()
  protected val edge_map: mutable.HashMap[Any, Edge] = mutable.HashMap()
//  protected val element_helper = new ElementHelper //[IdType]

  protected[memg] def add_element[V <: Element]
  (label: String, key_values: Seq[(String, Any)])(constr: (MemGraph, Any, String) => V): V = {
    val id = element_helper.new_id()
    val prop_kv = key_values.filterNot(x => (x._1 == PropId.id) || (x._1 == tplight.PropId.label))
    val element = constr(this, id, label)
    for ((k, v) <- prop_kv) {
      element.property(k.toString, v)
    }
    element
  }

  protected[memg] def insert_edge(edge: MemEdge): Unit = edge_map += (edge.id -> edge)

  override def add_vertex(label: String, key_values: (String, Any)*): Vertex = {
    val vertex = add_element(label, key_values)((_, eid, elabel) => new MemVertex(this, eid, elabel))
    vertex_map += (vertex.id -> vertex)
    vertex
  }

  override def vertices(vertex_ids: Any*): Iterator[Vertex] = {
    if (vertex_ids.isEmpty) {
      vertex_map.iterator.map(_._2)
    } else {
      vertex_ids.iterator.map(x => vertex_map.get(x)).collect {
        case Some(v) => v
      }

    }
  }

  override def edges(edge_ids: Any*): Iterator[Edge] = {
    if (edge_ids.isEmpty) {
      edge_map.iterator.map(_._2)
    } else edge_ids.iterator.map(x => edge_map.get(x)).collect {
      case Some(e) => e
    }

  }
}

object MemGraph {
  class EH extends ElementHelper {
    private var next_id: Int = 0
    def new_id(): Int = {
      next_id += 1
      next_id
    }
  }

  def apply(): MemGraph = new MemGraph(new EH)
}
