package sysmo.reform.db

import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import com.typesafe.config.Config
import org.apache.tinkerpop.gremlin.jsr223.JavaTranslator
import org.apache.tinkerpop.gremlin.orientdb.{OrientGraph, OrientGraphFactory}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{GraphTraversal, GraphTraversalSource}
import org.apache.tinkerpop.gremlin.process.traversal.{Bytecode, Order, P, Traversal}
import org.apache.tinkerpop.gremlin.structure.{Edge, T, Vertex}
import sysmo.reform.shared.data.{graph => G, table => sdt}
import sysmo.reform.shared.gremlin.GraphsonEncoder
import sysmo.reform.shared.{query => Q}
import sysmo.reform.shared.{data => D}
import sysmo.reform.util.FuncLogging
import upickle.default._

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try, Using}
import sdt.Printers._
import sysmo.reform.shared.data.graph.{EdgeSchema, ElementSchema}
import sysmo.reform.shared.data.table.Row
import sysmo.reform.shared.util.pprint
import sysmo.reform.util.Prog._

class GraphAppStorage(graph_factory: OrientGraphFactory, db_schema: G.DatabaseSchema) extends FuncLogging {
//  import sysmo.reform.shared.{data => RM}
//  val schema_map: Map[String, G.EntitySchema] = schemas.map(x => (x.name, x)).toMap
//  def entity_schema(klass: String): Option[G.EntitySchema] = schema_map.get(klass)
  def classes(graph: OrientGraph): Iterable[OClass] = graph.database.getMetadata.getSchema.getClasses.asScala
  def f_transactional[A](f: OrientGraph => Res[A]): Res[A] = {
    val graph = graph_factory.getTx
    val res = f(graph)
    res match {
      case Right(_) => graph.commit()
      case Left(_) => graph.rollback()
    }
    graph.close()
    res
  }

  def transactional[A](f: OrientGraph => A): Res[A] = {
    val graph = graph_factory.getTx
    try {
      val res = f(graph)
      graph.commit()
      ok(res)
    } catch {
      case e: Throwable => error(e)
    } finally {
      graph.close()
    }

  }
//  def create_schema(rec_meta_list: Seq[RecordMeta[_]]): Unit = {
//    logger.info("========== Creating classes ==========")
//    Using(graph_factory.getTx)  { graph =>
//      for (rec_meta <- rec_meta_list) {
//        if (graph.existClass(rec_meta.id))
//          logger.warn(f"Class ${rec_meta.id} already exists")
//        else
//          logger.info(f"Creating node class ${rec_meta.id}")
//          graph.createVertexClass(rec_meta.id)
//        val vertex_class = graph.database.getMetadata.getSchema.getClass(rec_meta.id)
//        vertex_class.setStrictMode(true)
//        rec_meta.field_keys.foreach(field => {
//          val prop_type = rec_meta.fields(field).tpe match {
//            case RM.StringType() => OType.STRING
//            case RM.IntegerType() => OType.INTEGER
//            case RM.RealType() => OType.DOUBLE
//            case RM.BoolType() => OType.BOOLEAN
//          }
//          vertex_class.createProperty(field.toString, prop_type)
//        })
//
//      }
//    }
//  }.get_options

  def create_entity_props(graph: OrientGraph, schema: ElementSchema, strict: Boolean): Unit = {
    val entity_class = graph.database.getMetadata.getSchema.getClass(schema.name)
    entity_class.setStrictMode(strict)
    schema.props.foreach(prop => {
      val prop_type = prop.tpe match {
        case D.StringType() => OType.STRING
        case D.IntegerType() => OType.INTEGER
        case D.RealType() => OType.DOUBLE
        case D.BoolType() => OType.BOOLEAN
        case D.DateType() => OType.DATE
        case D.DateTimeType() => OType.DATETIME
        case _ => throw new IllegalStateException(f"Cannot handle property ${prop}")
      }
      entity_class.createProperty(prop.name, prop_type)
    })
  }

  def create_vertex_link_props(graph: OrientGraph, schema: EdgeSchema): Unit = {
    def get_class(x: String) = graph.database.getMetadata.getSchema.getClass(x)
    val edge_class = get_class(schema.name)
    val from_class_name = schema.from.get.target.name
    val from_class = get_class(from_class_name)
    val to_class_name = schema.to.get.target.name
    val to_class = get_class(to_class_name)

    edge_class.createProperty(s"out", OType.LINK, from_class)
    edge_class.createProperty(s"in", OType.LINK, to_class)
    from_class.createProperty(s"out_${edge_class}", OType.LINK, edge_class)
    to_class.createProperty(s"in_${edge_class}", OType.LINK, edge_class)
  }

  def apply_schemas(): Res[Unit] = {
    logger.info("========== Creating database schema ==========")
    transactional { graph =>

      /** Create vertex classes */
      db_schema.vertex_schemas.foreach(schema_ref => {
        val schema = schema_ref.target
        if (graph.existClass(schema.name))
          throw new IllegalStateException(f"Class ${schema.name} already exists")
        else {
          logger.info(f"Creating vertex class ${schema.name}")
          graph.createVertexClass(schema.name)
          create_entity_props(graph, schema, true)
        }
      })

      /** Create edge classes */
      db_schema.edge_schemas.foreach(schema_ref => {
        val schema = schema_ref.target
        if (graph.existClass(schema.name))
          throw new IllegalStateException(f"Class ${schema.name} already exists")
        else {
          logger.info(f"Creating edge class ${schema.name}")
          graph.createEdgeClass(schema.name)
          create_entity_props(graph, schema, true)
          create_vertex_link_props(graph, schema)
        }
      })

    }
  }

  def drop_schema(): Res[Unit] = {
    logger.info("========== Dropping classes ==========")
    transactional { graph =>
      classes(graph).filter(k => k.isSubClassOf("V") && k.getName != "V").foreach(k => {
        logger.info(f"Dropping node class ${k.getName}")
        graph.database.getMetadata.getSchema.dropClass(k.getName)
      })
      classes(graph).filter(k => k.isSubClassOf("E") && k.getName != "E").foreach(k => {
        logger.info(f"Dropping edge class ${k.getName}")
        graph.database.getMetadata.getSchema.dropClass(k.getName)
      })
    }
  }

  def list_schema: Res[Unit] = {
    logger.info("========== Listing classes ==========")
    transactional { graph =>
      classes(graph).filter(k => k.isSubClassOf("V") && k.getName != "V")
        .foreach(k => logger.info(f"Node class ${k.getName}"))
      classes(graph).filter(k => k.isSubClassOf("E") && k.getName != "E")
        .foreach(k => logger.info(f"Edge class ${k.getName}"))
    }
  }

  def read_schema(klass: String): sdt.Schema = {
    transactional { graph => {
      val k = classes(graph).filter(k => k.getName == klass)
      if (k.isEmpty)
        return throw new IllegalArgumentException(f"Cannot find class $klass")
      val klass_def = k.head
      val fields = klass_def.properties().asScala.map(p => {
        val field_type = p.getType match {
          case OType.STRING => sdt.VectorType.Char
          case OType.INTEGER => sdt.VectorType.Int
          case OType.DOUBLE => sdt.VectorType.Real
          case OType.BOOLEAN => sdt.VectorType.Bool
          case OType.DATE => sdt.VectorType.Real
          case OType.DATETIME => sdt.VectorType.Real

          case x => throw new IllegalStateException(f"Cannot handle type $x")
        }
        val ext_class = p.getType match {
          case OType.DATE => sdt.Date
          case OType.DATETIME => sdt.DateTime
          case _ => sdt.Same
        }
        sdt.Field(p.getName, sdt.FieldType(field_type, ext_class = ext_class))
      }).toSeq
      sdt.Schema(fields)
    }}.get
  }

  def drop_data(): Res[Unit] = {
    logger.info("========== Dropping graph data ==========")
    transactional { graph =>
      graph.traversal().V().drop().iterate()
    }
  }

//  def import_batch[U](data: Seq[U])(implicit meta_source: RecordWithMeta[U]): Unit = {
//    val meta = meta_source._meta
//    val prop_args_init = List[Any](T.label, meta.id)
//
//    val graph = graph_factory.getNoTx
//    data.foreach(row => {
//      val prop_args = meta.field_keys.foldLeft(prop_args_init)((acc, item) =>
//        acc ++ Seq(meta.fields(item).name, meta.get_value(row, item))
//      )
//
//      graph.addVertex(prop_args: _*)
//    })
//    graph.close()
//  }

//  def insert_vertex(g: GraphTraversalSource, vertex_schema: G.VertexSchema, props: Row): Res[Unit] = {
//    val vertex_props = vertex_schema.props.foldLeft(
//      Seq[(String, Any)]()
//    )((acc, prop) => {
//      props.get(prop.name).v match {
//        case Some(x) => acc :+ (prop.name, x)
//        case None => acc
//      }
//    })
//    insert_vertex(g, vertex_schema, vertex_props)
//    val x : Map[String, String]
//    x.get()
//  }

//  case class UpdateStrategy(v: Int)
//  object UpdateStrategy {
//    object duplicate_error extends UpdateStrategy(0)
//    object duplicate_update extends UpdateStrategy(0)
//  }

//  def modify_vertices(schema: G.ElementSchema, table: sdt.Table,
//                      strategy: ElementModificationStrategy): Res[Unit] = {
//    f_transactional {graph =>
//      val g = graph.traversal()
//      table.row_iter.foldLeft(ok())((acc, row) =>
//        acc.flatMap(_ => ModifyElementOps.modify_vertex(g, schema, row, strategy).unit)
//      )
//    }
//  }

//  def insert_linked_vertices(edge_schema: G.EdgeSchema, to_other: Boolean, table: sdt.Table,
//                             this_vertex_strategy: ElementModificationStrategy,
//                             other_vertex_strategy: ElementFindingStrategy,
//                             edge_strategy: ElementModificationStrategy): Res[Unit] = {
//    f_transactional {graph =>
//      val g = graph.traversal()
//      table.row_iter.foldLeft(ok())((acc, row) =>
//        acc.flatMap(_ => ModifyElementOps.add_linked_vertex(
//          g, edge_schema, to_other, row, this_vertex_strategy, other_vertex_strategy, edge_strategy
//        ).unit)
//      )
//    }
//  }
  type RowTraversalSourceFn[S, B] = (GraphTraversalSource, sdt.Row) => GraphTraversal[S, B]
  type RowTraversalFn[S, A, B] = (GraphTraversal[S, A], sdt.Row) => GraphTraversal[S, B]

  def insert_vertices(table: sdt.Table, create: RowTraversalSourceFn[Vertex, Vertex]): Res[Unit] = {
    f_transactional {graph =>
      val g = graph.traversal()
      table.row_iter.foldLeft(ok())((acc, row) =>
        acc.map(_ => create(g, row).next())
      )
    }
  }

  def insert_linked_vertices(table: sdt.Table,
                             from: RowTraversalSourceFn[Vertex, Vertex],
                             to: RowTraversalFn[Vertex, Vertex, Vertex],
                             edge: RowTraversalFn[Vertex, Vertex, Edge]): Res[Unit] = {
    f_transactional { graph =>
      val g = graph.traversal()
      table.row_iter.foldLeft(ok())((acc, row) =>
        acc.map(trav => from(g, row).as("from"))
          .map(trav => to(trav, row).as("to"))
          .map(trav => {
//            val final_trav = trav
            val final_trav = edge(trav, row)
            println(GremlinIO.writeValueAsString(final_trav))
            final_trav.next()
          })
      )
    }
  }


  def query_table(q: Q.Query, tm: sdt.TableManager): sdt.Table = {
    val gremlin_scala_bc = Q.Query2GremlinCompiler.compile(q)
    val gremlin_str = write(GraphsonEncoder.to_value(gremlin_scala_bc))
//    logger.info(gremlin_str)
    val gremlin_native_bc = GremlinIO.readValue(gremlin_str, classOf[Bytecode])
    Using(graph_factory.getTx) { graph =>
      val g = graph.traversal
      val trav: Traversal.Admin[_, _] = JavaTranslator.of(g).translate(gremlin_native_bc)
      val schema = q match {
        case Q.BasicQuery(source, columns_opt, _, _, _) => source match {
          case Q.SingleTable(id, _, _) => {
            columns_opt.map(columns => {
              val db_table_schema: sdt.Schema = db_schema.vertex_schema(id) match {
                case Some(sch: G.VertexSchema) => G.Schema.table_schema_builder(sch).build
                case _ => throw new IllegalArgumentException(s"No VertexSchema named $id")
              }

              columns.map(col => db_table_schema.field(col.id).get)
            }).map(columns => sdt.Schema(columns)).orElse(Some(read_schema(id))).get
          }
          case _ => throw new IllegalArgumentException("Can only handle query with a single table source")
        }
        case _ => throw new IllegalArgumentException("Can only handle basic query")
      }
      val builder = tm.incremental_table_builder(schema)

      trav.asScala.foreach(x => {
        val prop_map = x.asInstanceOf[java.util.Map[String, Any]].asScala
          .toMap.view.mapValues {
            case el: java.util.List[_] => Some(el.get(0))
            case x => Some(x)
          }.toMap
        builder :+ prop_map
      })

      builder.toTable
    }.get
  }

}

object GraphAppStorage {
  def apply(config: Config, db_schema: G.DatabaseSchema): GraphAppStorage = {
    val user = config.getString("user")
    val password = config.getString("password")
    val uri = config.getString("uri")
    val graph_factory = new OrientGraphFactory(uri, user, password)
    new GraphAppStorage(graph_factory, db_schema)
  }
}