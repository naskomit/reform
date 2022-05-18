package sysmo.reform.shared.gremlin.memg

import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Vertex}

case class MemEdge(graph: MemGraph, id: Any, label: String, in_vertex: MemVertex, out_vertex: MemVertex)
  extends MemElement with Edge {
  override def vertices(direction: Direction): Seq[Vertex] = {
    direction match {
      case Direction.IN => Seq(in_vertex)
      case Direction.OUT => Seq(out_vertex)
      case Direction.BOTH => Seq(out_vertex, in_vertex)
    }
  }

  override def toString: String = s"$out_vertex-e[$id:$label]->$in_vertex"
}
