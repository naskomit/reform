package sysmo.reform.data.graph

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{GraphTraversal, GraphTraversalSource}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.__
import org.apache.tinkerpop.gremlin.structure.{Edge, Vertex}


object CompositeTraversals {
  class TraversalBuilderStart[Data](g: GraphTraversalSource, data: Data) {
    type TravFn = (GraphTraversalSource, Data) => GraphTraversal[_, _]
    def start(f: TravFn): TraversalBuilder[Data] =
      new TraversalBuilder[Data](f(g, data), data)
    def start(id: String, f: TravFn): TraversalBuilder[Data] =
      new TraversalBuilder[Data](f(g, data).as(id), data)
  }

  class TraversalBuilder[Data](trav: GraphTraversal[_, _], data: Data) {
    type TravFn = (GraphTraversal[_, _], Data) => GraphTraversal[_, _]
    def extend(f: TravFn): TraversalBuilder[Data] =
      new TraversalBuilder[Data](f(trav, data), data)
    def extend(id: String, f: TravFn): TraversalBuilder[Data] =
      new TraversalBuilder[Data](f(trav, data).as(id), data)
    def build: GraphTraversal[_, _] = trav
  }

}
