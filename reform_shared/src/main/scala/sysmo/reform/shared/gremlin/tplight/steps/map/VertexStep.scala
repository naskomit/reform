package sysmo.reform.shared.gremlin.tplight.steps.map

import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Element, Traversal, Traverser, Vertex}

class VertexStep[E <: Element](direction: Direction, edge_labels: Seq[String])
                              (implicit iter_supplier: (Traverser[Vertex], Direction, Seq[String]) => Iterator[E])
    extends FlatMapStep[Vertex, E] {

  override def flat_map(traverser: Traverser[Vertex]): Iterator[E] =
    iter_supplier(traverser, direction, edge_labels)

}

object VertexStep {
  implicit val vertex_iter_supplier: (Traverser[Vertex], Direction, Seq[String]) => Iterator[Vertex] =
    (trav, dir, edge_labels) =>
      trav.get.map(x =>
        x.vertices(dir, edge_labels)
      ).getOrElse(Iterator.empty)

  implicit val edge_iter_supplier: (Traverser[Vertex], Direction, Seq[String]) => Iterator[Edge] =
    (trav, dir, edge_labels) => trav.get.map(_.edges(dir, edge_labels)).getOrElse(Iterator.empty)
}
