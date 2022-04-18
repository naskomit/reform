package sysmo.reform.data.graph

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{GraphTraversal, GraphTraversalSource, __}
import org.apache.tinkerpop.gremlin.structure.{Edge, Vertex}
import sysmo.reform.shared.data.graph.VertexSchema
import sysmo.reform.shared.data.{graph => G, table => sdt}

object Implicits {
  def b_create_vertex(schema: G.VertexSchema, row: sdt.Row): GraphTraversal[Nothing, Vertex] = {
    __.addV(schema.name).b_append_props(schema, row)
  }



  implicit class InitialTraversalBuilder(g: GraphTraversalSource) {
    def b_create_vertex(schema: G.VertexSchema, row: sdt.Row): GraphTraversal[Vertex, Vertex] = {
      g.addV(schema.name).b_append_props(schema, row)
    }
  }

  implicit class TraversalBuilder[S, E](trav: GraphTraversal[S, E]) {
    def b_append_prop(prop_id: String, prop_values: sdt.Row): GraphTraversal[S, E] = {
      prop_values.get(prop_id).v match {
        case Some(x) => trav.property(prop_id, x)
        case None => trav
      }
    }

    def b_append_props(schema: G.ElementSchema, prop_values: sdt.Row): GraphTraversal[S, E] =
      schema.props.foldLeft(trav)(
        (acc, prop) => acc.b_append_prop(prop.name, prop_values)
      )

//    def b_or_create_vertex(schema: G.VertexSchema): GraphTraversal[S, _] = {
//      trav.fold().coalesce(
//        __.unfold(),
//        __.addV(schema.name)
//      )
//    }

    type TraversalFn0[R] = Function0[GraphTraversal[_, R]]
    type TraversalFn1[P1, R] = Function1[GraphTraversal[S, P1], GraphTraversal[S, R]]


    def b_find_or_create_vertex(find: GraphTraversal[S, Vertex], schema: G.VertexSchema, row: sdt.Row): GraphTraversal[S, Vertex] = {
      trav.coalesce(
        find,
        __.addV(schema.name).b_append_props(schema, row)
      )
    }

    def b_update_or_create_vertex(find: GraphTraversal[S, Vertex], schema: G.VertexSchema, row: sdt.Row): GraphTraversal[S, Vertex] = {
      trav.coalesce(
        find,
        __.addV(schema.name)
      ).b_append_props(schema, row)
    }

    def b_update_or_create_edge(find: GraphTraversal[S, Edge], schema: G.EdgeSchema, row: sdt.Row, from: String, to: String): GraphTraversal[S, Edge] = {
      trav.coalesce(
        find,
        __.addE(schema.name).from(from).to(to)
      ).b_append_props(schema, row)
    }
  }
}
