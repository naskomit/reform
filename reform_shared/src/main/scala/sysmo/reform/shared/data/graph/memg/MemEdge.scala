package sysmo.reform.shared.data.graph.memg

import sysmo.reform.shared.data.graph.tplight.{Direction, Edge, Graph, Property, Vertex}

import scala.collection.mutable

case class MemEdge[IdType](graph: MemGraph[IdType], id: IdType, label: String, in_vertex: MemVertex[IdType], out_vertex: MemVertex[IdType])
  extends MemElement[IdType] with Edge[IdType] {

  override def vertices(direction: Direction): Seq[Vertex[IdType]] = {
    direction match {
      case Direction.IN => Seq(in_vertex)
      case Direction.OUT => Seq(out_vertex)
      case Direction.BOTH => Seq(out_vertex, in_vertex)
    }
  }

  override def toString: String = s"$out_vertex-e[$id:$label]->$in_vertex"
}
