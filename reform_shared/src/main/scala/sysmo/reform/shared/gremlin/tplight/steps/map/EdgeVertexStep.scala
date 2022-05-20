package sysmo.reform.shared.gremlin.tplight.steps.map

import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, GraphTraversalBuilder, Traversal, Traverser, Vertex}

class EdgeVertexStep(direction: Direction)
    extends FlatMapStep[Edge, Vertex] {
  override def flat_map(traverser: Traverser[Edge]): Iterator[Vertex] =
    traverser.get.vertices(direction)
}
