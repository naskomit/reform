package sysmo.reform.db

import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import com.typesafe.config.Config
import org.apache.tinkerpop.gremlin.jsr223.JavaTranslator
import org.apache.tinkerpop.gremlin.orientdb.{OrientGraph, OrientGraphFactory}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.process.traversal.step.util.WithOptions
import org.apache.tinkerpop.gremlin.process.traversal.{Bytecode, Order, P, Traversal}
import org.apache.tinkerpop.gremlin.structure.T
import sysmo.coviddata.CSVDataSource
import sysmo.coviddata.shared.data.PatientRecord
import sysmo.reform.shared.data.{DummyOptionProvider, RecordMeta, RecordWithMeta, graph => G, table => sdt}
import sysmo.reform.shared.gremlin.GraphsonEncoder
import sysmo.reform.shared.{query => Q}
import sysmo.reform.util.Logging
import upickle.default._

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try, Using}
import sdt.Printers._
import sysmo.reform.shared.util.pprint

class GraphAppStorage(graph_factory: OrientGraphFactory) extends Logging {
  import sysmo.reform.shared.{data => RM}

  def classes(graph: OrientGraph): Iterable[OClass] = graph.database.getMetadata.getSchema.getClasses.asScala

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
//  }.get

  def apply_schemas(schemas: Seq[G.EntitySchema]): Unit = {
    logger.info("========== Creating database schema ==========")
    Using(graph_factory.getTx) { graph =>
      for (schema <- schemas) {
        if (graph.existClass(schema.name))
          throw new IllegalStateException(f"Class ${schema.name} already exists")
//          logger.warn()
        else {
          schema match {
            case x: G.VertexSchema => {
              logger.info(f"Creating vertex class ${schema.name}")
              graph.createVertexClass(schema.name)
            }
            case x: G.EdgeSchema => {
              logger.info(f"Creating edge class ${schema.name}")
              graph.createEdgeClass(schema.name)
            }
          }

          val vertex_class = graph.database.getMetadata.getSchema.getClass(schema.name)
          vertex_class.setStrictMode(true)
          schema.props.foreach(prop => {
            val prop_type = prop.prop_type match {
              case G.StringType() => OType.STRING
              case G.IntegerType() => OType.INTEGER
              case G.RealType() => OType.DOUBLE
              case G.BoolType() => OType.BOOLEAN
              case G.DateType() => OType.DATE
              case G.DateTimeType() => OType.DATETIME
              case _ => throw new IllegalStateException(f"Cannot handle property ${prop}")
            }
            vertex_class.createProperty(prop.name, prop_type)
          })

        }



      }
    }.get
  }

  def drop_schema: Unit = {
    logger.info("========== Dropping classes ==========")
    Using(graph_factory.getTx) { graph =>
      classes(graph).filter(k => k.isSubClassOf("V") && k.getName != "V").foreach(k => {
        logger.info(f"Dropping node class ${k.getName}")
        graph.database.getMetadata.getSchema.dropClass(k.getName)
      })
      classes(graph).filter(k => k.isSubClassOf("E") && k.getName != "E").foreach(k => {
        logger.info(f"Dropping edge class ${k.getName}")
        graph.database.getMetadata.getSchema.dropClass(k.getName)
      })
    }.get
  }

  def list_schema: Unit = {
    logger.info("========== Listing classes ==========")
    Using(graph_factory.getTx) { graph =>
      classes(graph).filter(k => k.isSubClassOf("V") && k.getName != "V")
        .foreach(k => logger.info(f"Node class ${k.getName}"))
      classes(graph).filter(k => k.isSubClassOf("E") && k.getName != "E")
        .foreach(k => logger.info(f"Edge class ${k.getName}"))
    }.get
  }

  def read_schema(klass: String): sdt.Schema = {
    Using(graph_factory.getNoTx) { graph => {
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

  def drop_data: Unit = {
    logger.info("========== Dropping graph data ==========")
    Using(graph_factory.getTx) { graph =>
      graph.traversal().V().drop().iterate()
    }.get
  }

  def import_batch[U](data: Seq[U])(implicit meta_source: RecordWithMeta[U]): Unit = {
    val meta = meta_source._meta
    val prop_args_init = List[Any](T.label, meta.id)

    val graph = graph_factory.getNoTx
    data.foreach(row => {
      val prop_args = meta.field_keys.foldLeft(prop_args_init)((acc, item) =>
        acc ++ Seq(meta.fields(item).name, meta.get_value(row, item))
      )

      graph.addVertex(prop_args: _*)
    })
    graph.close()
  }

  def upsurt_vertices(graph_schema: G.VertexSchema, table: sdt.Table, id_column: String): Unit = {
    val graph = graph_factory.getTx
    val g = graph.traversal()
    for (row <- table.row_iter) {
      val vertex_props = table.schema.fields.foldLeft(
        Seq[(String, Any)]()
      )((acc, field) => {
        graph_schema.prop(field.name).map(prop => {
          row.get(field.name).v match {
            case Some(x) => acc :+ (prop.name, x)
            case None => acc
          }
        }).getOrElse(acc)
      })

      row.get(id_column).v match {
        case None => {
          println(row)
          throw new IllegalStateException(f"No value for `id` attribute in column $id_column")
        }
        case Some(id_value) => {
          val vertex_trav = g.V().has(T.label, graph_schema.name).has(id_column, id_value)
          val vertex = if (vertex_trav.hasNext) {
            logger.info(f"Updating vetex with `$id_column` = ${row.get(id_column).v.get}")
            vertex_trav.next()
          } else {
            logger.info(f"Adding vetex with `$id_column` = ${row.get(id_column).v.get}")
            graph.addVertex(List[Any](T.label, graph_schema.name): _*)
          }
          vertex_props.foldLeft(g.V(vertex))((trav, kv) => trav.property(kv._1, kv._2)).iterate()
        }
      }

    }
    graph.commit()
    graph.close()
  }

  def query_table(q: Q.Query, tm: sdt.TableManager): sdt.Table = {
    val gremlin_scala_bc = Q.Query2GremlinCompiler.compile(q)
    val gremlin_str = write(GraphsonEncoder.to_value(gremlin_scala_bc))
    val gremlin_native_bc = GremlinIO.readValue(gremlin_str, classOf[Bytecode])
    Using(graph_factory.getTx) { graph =>
      val g = graph.traversal
      val trav: Traversal.Admin[_, _] = JavaTranslator.of(g).translate(gremlin_native_bc)
      val schema = q match {
        case Q.BasicQuery(source, columns_opt, _, _, _) => source match {
          case Q.SingleTable(id, _, _) => {
            columns_opt.map(columns => {
              val db_table_schema = read_schema(id)
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
  def apply(config: Config): GraphAppStorage = {
    val user = config.getString("user")
    val password = config.getString("password")
    val uri = config.getString("uri")
    val graph_factory = new OrientGraphFactory(uri, user, password)
    new GraphAppStorage(graph_factory)
  }
}