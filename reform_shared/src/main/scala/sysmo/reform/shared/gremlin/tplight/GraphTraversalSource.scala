package sysmo.reform.shared.gremlin.tplight

import sysmo.reform.shared.gremlin.bytecode.Symbols
import sysmo.reform.shared.gremlin.tplight.steps.GraphStep
import sysmo.reform.shared.gremlin.tplight.steps.GraphStep._

trait TraversalSource {

}

case class GraphTraversalSource(graph: Graph, bytecode: Bytecode) extends TraversalSource {
  def V(vertex_ids: Any*): GraphTraversal[Vertex, Vertex] = {
//    val new_bytecode = bytecode.add_step(Symbols.V, vertex_ids)
//    val new_traversal = new GraphTraversal(graph, new_bytecode, )
    val new_traversal = new GraphTraversal.Empty(graph)
    new_traversal.add_step(
      new GraphStep[Vertex, Vertex](new_traversal, vertex_ids),
      bc => bc.add_step(Symbols.V, vertex_ids)
    )
  }
}
