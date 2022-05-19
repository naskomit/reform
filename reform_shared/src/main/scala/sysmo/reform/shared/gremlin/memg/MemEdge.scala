package sysmo.reform.shared.gremlin.memg

import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Vertex}

case class MemEdge(graph: MemGraph, id: Any, label: String, out_vertex: MemVertex, in_vertex: MemVertex)
  extends MemElement with Edge {
  override def vertices(direction: Direction): Iterator[Vertex] = {
    direction match {
      case Direction.IN => Seq(in_vertex).iterator
      case Direction.OUT => Seq(out_vertex).iterator
      case Direction.BOTH => Seq(out_vertex, in_vertex).iterator
    }
  }

  override def toString: String = s"$out_vertex-e[$id:$label]->$in_vertex"
}
