package sysmo.reform.shared.gremlin.tplight

import sysmo.reform.shared.gremlin.bytecode.Symbols
import sysmo.reform.shared.gremlin.tplight.steps.GraphStep
import sysmo.reform.shared.gremlin.tplight.steps.GraphStep._

trait TraversalSource {

}

case class GraphTraversalSource(graph: Graph, bytecode: Bytecode) extends TraversalSource {
  def V(vertex_ids: Any*): GraphTraversalBuilder[Vertex, Vertex] = {
    val builder: GraphTraversalBuilder[Vertex, Nothing] = new GraphTraversalBuilder(graph)
    builder.add_step(
      new GraphStep[Vertex](vertex_ids),
      bc => bc.add_step(Symbols.V, vertex_ids)
    )
  }
}
