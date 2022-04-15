package sysmo.reform.db

//import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{GraphTraversal, GraphTraversalSource}
//import org.apache.tinkerpop.gremlin.structure.{Edge, Element, T, Vertex}
//import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
//import sysmo.reform.shared.data.{graph => G, table => sdt}
//import sysmo.reform.util.Prog._
//
//sealed trait ElementStrategy
//
//sealed trait ElementModificationStrategy extends ElementStrategy
//case object Create extends ElementModificationStrategy
//case class CreateOrError(id_columns: String*) extends ElementModificationStrategy
//case class UpdateIfExists(id_columns: String*) extends ElementModificationStrategy
//case class UpdateOrError(id_columns: String*) extends ElementModificationStrategy
//case class CreateOrUpdate(id_columns: String*) extends ElementModificationStrategy
//
//sealed trait ElementFindingStrategy extends ElementStrategy {
//  val id_columns: Seq[String]
//  def value_id(value_map: sdt.Row) = {
//    "{" + id_columns.map(x => s"$x -> ${value_map.get(x).v.getOrElse("NULL")}").mkString(",") + "}"
//  }
//}
//case class FindOrSkip(id_columns: String*) extends ElementFindingStrategy
//case class FindOrCreate(id_columns: String*) extends ElementFindingStrategy
//case class FindOrError(id_columns: String*) extends ElementFindingStrategy
//
//class ModifyElementOps(g: GraphTraversalSource, schema: G.ElementSchema, prop_values: sdt.Row) {
//  type Traversal = GraphTraversal[_,_]
//
//  implicit class TraversalBuilder[S, E](trav: GraphTraversal[S, E]) {
//    def b_append_prop(prop_id: String): GraphTraversal[S, E] = {
//      prop_values.get(prop_id).v match {
//        case Some(x) => trav.property(prop_id, x)
//        case None => trav
//      }
//    }
//
//    def b_append_props: GraphTraversal[S, E] =
//      schema.props.foldLeft(trav)(
//        (acc, prop) => acc.b_append_prop(prop.name)
//      )
//
//    def b_or_create_vertex: GraphTraversal[_, _] = {
//      trav.fold().coalesce(
//        __.unfold(),
//        add_vertex.get
//      )
//    }
//
//  }
//  // g.V().has('1', 183).has(label, 'SocioDemographic').as('source').V().has('1', 183).has(label, 'HasClinical').as('dest').addE('Clinical').from('source').to('dest')
//  // g.V().has('1', 183).has(label, 'SocioDemographic').outE()
//
//  protected def add_vertex: Res[GraphTraversal[_, _]] = schema match {
//    case _: G.VertexSchema => ok(g.addV(schema.name))
//    case _: G.EdgeSchema => error(new IllegalArgumentException("Not an edge schema"))
//  }
//
//  protected def add_edge(from: String, to: String): Res[GraphTraversal[_, _]] = schema match {
//    case _: G.VertexSchema => error(new IllegalArgumentException("Not a vertex schema"))
//    case _: G.EdgeSchema => ok(g.addE(schema.name).from(from).to(to))
//  }
//
//  protected def add_edge(from: Vertex, to: Vertex): Res[GraphTraversal[_, _]] = schema match {
//    case _: G.VertexSchema => error(new IllegalArgumentException("Not a vertex schema"))
//    case _: G.EdgeSchema => ok(g.addE(schema.name).from(from).to(to))
//  }
//
//  def find_element(id_columns: Seq[String]): Res[Traversal] = {
//    val start: Res[Traversal] = Right(
//      schema match {
//        case _: G.VertexSchema => g.V().has(T.label, schema.name)
//        case _: G.EdgeSchema => g.E().has(T.label, schema.name)
//      }
//    )
//    id_columns.foldLeft(start)((acc, id_column) => {
//      acc.flatMap(trav => {
//        prop_values.get(id_column).v match {
//          case Some(x) => Right(trav.has(id_column, x))
//          case None => Left(new IllegalStateException(f"No value for `id` attribute in column $id_column"))
//        }
//      })
//    })
//    //      traversal.map(x => if (x.hasNext) Some(x.next()) else None)
//  }
//
//  def insert_vertex: Res[GraphTraversal[_, _]] = add_vertex.map(_.b_append_props)
//
//  def insert_edge(from: String, to: String): Res[GraphTraversal[_, _]] = add_edge(from, to).map(_.b_append_props)
//  def insert_edge(from: Vertex, to: Vertex): Res[GraphTraversal[_, _]] = add_edge(from, to).map(_.b_append_props)
//
//  def update_element(id_columns: Seq[String]): Res[GraphTraversal[_, _]] =
//    find_element(id_columns).map(trav => trav.b_append_props)
//
//  def upsert_vertex(id_columns: Seq[String]): Res[GraphTraversal[_, _]] =
//    find_element(id_columns).map(trav => trav.b_or_create_vertex.b_append_props)
//
//}
//
//object ModifyElementOps {
//  implicit class TraversalFinalizer[S, E](trav: GraphTraversal[S, E]) {
//    def b_element: Element = trav.next().asInstanceOf[Element]
//    def b_vertex: Vertex = trav.next().asInstanceOf[Vertex]
//    def b_edge: Edge = trav.next().asInstanceOf[Edge]
//  }
//
//  def modify_vertex(g: GraphTraversalSource, schema: G.ElementSchema, row: sdt.Row,
//                    strategy: ElementModificationStrategy): Res[Vertex] = {
//
//    val ops = new ModifyElementOps(g, schema, row)
//
//    strategy match {
//      case Create => ops.insert_vertex.map(_.b_vertex)
//
//      case CreateOrError(id_columns @ _*) => {
//        ops.find_element(id_columns)
//          .map(x => if (x.hasNext) Some(x.next()) else None).flatMap {
//          case Some(_) => Left(new RuntimeException("Vertex already exists!"))
//          case None => ops.insert_vertex.map(_.b_vertex)
//        }
//      }
//
//      case UpdateIfExists(id_columns@ _*) => {
//        // TODO Check if hasNext and catch exception
//        ops.update_element(id_columns).map(_.b_vertex)
//      }
//
//      case UpdateOrError(id_columns @ _*) => {
//        ops.update_element(id_columns).map(_.b_vertex)
//      }
//
//      case CreateOrUpdate(id_columns @ _*) => {
//        ops.upsert_vertex(id_columns).map(_.b_vertex)
//      }
//    }
//  }
//
//  def find_vertex(g: GraphTraversalSource, schema: G.ElementSchema, row: sdt.Row, strategy: ElementFindingStrategy): Res[Option[Vertex]] = {
//    val ops = new ModifyElementOps(g, schema, row)
//    import ops._
//
//    strategy match {
//      case FindOrSkip(id_columns@_*) => ops.find_element(id_columns).flatMap(t => if (t.hasNext) ok(Some(t.b_vertex)) else ok(None))
//      case FindOrCreate(id_columns@_*) => ops.find_element(id_columns).map(t => Some(t.b_or_create_vertex.b_vertex))
//      case FindOrError(id_columns@_*) =>
//        ops.find_element(id_columns).flatMap(
//          t => {
//            if (t.hasNext)
//              ok(Some(t.b_vertex))
//            else
//              error(new IllegalArgumentException(s"Cannot find element ${strategy.value_id(row)}"))
//          }
//        )
//    }
//   }
//
//  def add_linked_vertex(g: GraphTraversalSource, edge_schema: G.EdgeSchema,
//                        to_other: Boolean,
//                        row: sdt.Row,
//                        this_vertex_strategy: ElementModificationStrategy,
//                        other_vertex_strategy: ElementFindingStrategy,
//                        edge_strategy: ElementModificationStrategy): Res[(Vertex, Edge)] = {
//
//    val (this_vertex_schema, other_vertex_schema) = if (to_other)
//      (edge_schema.from.get.target, edge_schema.to.get.target)
//    else
//      (edge_schema.to.get.target, edge_schema.from.get.target)
//
//    val this_vertex_res: Res[Vertex] = modify_vertex(g, this_vertex_schema, row, this_vertex_strategy)
//    val other_vertex_res: Res[Option[Vertex]] = find_vertex(g, other_vertex_schema, row, other_vertex_strategy)
//    val edge_ops = new ModifyElementOps(g, edge_schema, row)
//
//    import edge_ops._
//
//    val edge_res: Res[Edge] = (this_vertex_res, other_vertex_res) match {
//      case (Right(this_vertex), Right(other_vertex)) => {
//        val edge_trav = if (to_other)
//          edge_ops.add_edge(this_vertex, other_vertex.get)
//        else
//          edge_ops.add_edge(other_vertex.get, this_vertex)
//
//        edge_trav.map(_.b_append_props.b_edge)
//      }
//
//      case (Left(err), _)  => Left(err)
//      case (_, Left(err)) => Left(err)
//    }
//
//    val res: Res[(Vertex, Edge)] = (this_vertex_res, edge_res) match {
//      case (Right(vertex), Right(edge)) => Right(vertex, edge)
//      case (Left(err), _)  => Left(err)
//      case (_, Left(err)) => Left(err)
//    }
//
//    res
//  }
//
//
//}
