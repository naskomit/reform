package sysmo.reform.shared.gremlin.memg

import sysmo.reform.shared.gremlin.tplight.Property
import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Graph, PropId, Vertex}

import scala.collection.mutable

case class MemVertex(graph: MemGraph, id: Any, label: String) extends MemElement with Vertex {
  val out_edges: mutable.HashMap[String, Set[Edge]] = mutable.HashMap()
  val in_edges: mutable.HashMap[String, Set[Edge]] = mutable.HashMap()

  override def vertices(direction: Direction, edge_labels: Seq[String]): Iterator[Vertex] = {
    val res_builder = mutable.ArrayBuffer[Vertex]()

    if (direction == Direction.IN || direction == Direction.BOTH) {
      if (edge_labels.isEmpty)
        res_builder ++= in_edges.toSeq.flatMap(_._2.map(_.out_vertex))
      else
        edge_labels.collect {
          case l if in_edges.contains(l) => res_builder ++= in_edges(l).map(_.out_vertex)
        }
    }

    if (direction == Direction.OUT || direction == Direction.BOTH) {
      if (edge_labels.isEmpty)
        res_builder ++= out_edges.toSeq.flatMap(_._2.map(_.in_vertex))
      else
        edge_labels.collect {
          case l if out_edges.contains(l) => res_builder ++= out_edges(l).map(_.in_vertex)
        }
    }

    res_builder.toSeq.iterator
  }

  override def edges(direction: Direction, edge_labels: Seq[String]): Iterator[Edge] = {
    val res_builder = mutable.ArrayBuffer[Edge]()

    if (direction == Direction.IN || direction == Direction.BOTH) {
      if (edge_labels.isEmpty)
        res_builder ++= in_edges.toSeq.flatMap(_._2)
      else
        edge_labels.collect {
          case l if in_edges.contains(l) => res_builder ++= in_edges(l)
        }
    }

    if (direction == Direction.OUT || direction == Direction.BOTH) {
      if (edge_labels.isEmpty)
        res_builder ++= out_edges.toSeq.flatMap(_._2)
      else
        edge_labels.collect {
          case l if out_edges.contains(l) => res_builder ++= out_edges(l)
        }
    }

    res_builder.toSeq.iterator
  }

  override def add_edge(label: String, to: Vertex, key_values: (String, Any)*): Edge = {
    to match {
      case mto: MemVertex => graph.add_element(label, key_values)((_, eid, elabel) => {
        val edge = new MemEdge(graph, eid, elabel, this, mto)
        out_edges += (label -> (out_edges.getOrElse(label, Set[Edge]()) + edge))
        mto.in_edges += (label -> (mto.in_edges.getOrElse(label, Set[Edge]()) + edge))
        graph.insert_edge(edge)
        edge
      })
    }

  }

  override def toString: String = s"v[$id:$label]"

}
