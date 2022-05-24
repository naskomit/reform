package sysmo.reform.shared.data.form4

import sysmo.reform.shared.gremlin.tplight.{Edge, Graph, GraphTraversalBuilder, GraphTraversalSource, Vertex}

trait GraphObject {
  def graph: Graph
  def g: GraphTraversalSource = graph.traversal()
}

trait ElementObj extends GraphObject {
}

trait VertexObj extends ElementObj {
  val vertex: Vertex
  def graph: Graph = vertex.graph
  def g_this: GraphTraversalBuilder[Vertex, Vertex] = g.V(vertex.id)
}

trait EdgeObj extends ElementObj {
  val edge: Edge
  def graph: Graph = edge.graph
}
