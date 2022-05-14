package sysmo.reform.shared.data.graph.tplight

trait TraversalSource {

}

class GraphTraversalSource(val graph: Graph) extends TraversalSource {
  val bytecode: Bytecode = new Bytecode
  def V(vertex_ids: Any*): GraphTraversal[Vertex] = {
    new GraphTraversal()
  }
}
