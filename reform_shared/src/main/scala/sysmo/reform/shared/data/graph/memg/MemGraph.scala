package sysmo.reform.shared.data.graph.memg

import sysmo.reform.shared.data.graph.{tplight => TPL}

import scala.collection.mutable


trait ElementHelper {
  def new_id(): Any
}

class MemGraph(element_helper: ElementHelper) extends TPL.Graph {
  protected val vertex_map: mutable.HashMap[Any, TPL.Vertex] = mutable.HashMap()
  protected val edge_map: mutable.HashMap[Any, TPL.Edge] = mutable.HashMap()
//  protected val element_helper = new ElementHelper //[IdType]

  protected[memg] def add_element[V <: TPL.Element]
  (label: String, key_values: Seq[Tuple2[TPL.PropId, Any]])(constr: (MemGraph, Any, String) => V): V = {
    val id = element_helper.new_id()
//    val label: Option[String] = Some(key_values.collect {case (k, v) if k == TPL.PropId.label => v})
//      .flatMap {
//        case x if x.size == 1 && x.head.isInstanceOf[String] => Some(x.head.asInstanceOf[String])
//        case _ => None
//      }
//    if (label.isEmpty) {
//      throw new IllegalArgumentException(s"No proper label specified for vertex $key_values")
//    }

    val prop_kv = key_values.filterNot(x => (x._1 == TPL.PropId.id) || (x._1 == TPL.PropId.label))
    val element = constr(this, id, label)
    for ((k, v) <- prop_kv) {
      element.property(k.toString, v)
    }
    element
  }

  protected[memg] def insert_edge(edge: MemEdge): Unit = edge_map += (edge.id -> edge)

  override def add_vertex(label: String, key_values: Tuple2[TPL.PropId, Any]*): TPL.Vertex = {
    val vertex = add_element(label, key_values)((_, eid, elabel) => new MemVertex(this, eid, elabel))
    vertex_map += (vertex.id -> vertex)
    vertex
  }

  override def vertices(vertex_ids: Any*): Iterator[TPL.Vertex] = {
    if (vertex_ids.isEmpty) {
      vertex_map.iterator.map(_._2)
    } else {
      vertex_ids.iterator.map(x => vertex_map.get(x)).collect {
        case Some(v) => v
      }

    }
  }

  override def edges(edge_ids: Any*): Iterator[TPL.Edge] = {
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
