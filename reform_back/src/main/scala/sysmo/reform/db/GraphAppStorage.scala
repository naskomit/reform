package sysmo.reform.db

import com.orientechnologies.orient.core.metadata.schema.OType
import com.typesafe.config.Config
import org.apache.tinkerpop.gremlin.jsr223.JavaTranslator
import org.apache.tinkerpop.gremlin.orientdb.{OrientGraph, OrientGraphFactory}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.process.traversal.step.util.WithOptions
import org.apache.tinkerpop.gremlin.process.traversal.{Bytecode, P, Traversal, Order}
import org.apache.tinkerpop.gremlin.structure.T
import sysmo.coviddata.CSVDataSource
import sysmo.coviddata.shared.data.PatientRecord
import sysmo.reform.shared.data.{RecordMeta, RecordWithMeta}
import sysmo.reform.shared.gremlin.GraphsonEncoder
import sysmo.reform.shared.query.{Query, Query2GremlinCompiler}
import sysmo.reform.util.Logging
import upickle.default._

import scala.jdk.CollectionConverters._
import scala.util.Using

class GraphAppStorage(graph_factory: OrientGraphFactory) extends Logging {
  import sysmo.reform.shared.{data => RM}

  def classes(graph: OrientGraph) = graph.database.getMetadata.getSchema.getClasses.asScala

  def create_schema(rec_meta_list: Seq[RecordMeta[_]]) = {
    logger.info("========== Creating classes ==========")
    Using(graph_factory.getTx)  { graph =>
      for (rec_meta <- rec_meta_list) {
        if (graph.existClass(rec_meta.id))
          logger.warn(f"Class ${rec_meta.id} already exists")
        else
          logger.info(f"Creating node class ${rec_meta.id}")
          graph.createVertexClass(rec_meta.id)
        val vertex_class = graph.database.getMetadata.getSchema.getClass(rec_meta.id)
        vertex_class.setStrictMode(true)
        rec_meta.field_keys.foreach(field => {
          val prop_type = rec_meta.fields(field).tpe match {
            case RM.StringType() => OType.STRING
            case RM.IntegerType() => OType.INTEGER
          }
          vertex_class.createProperty(field.toString, prop_type)
        })

      }
    }
  }.get

  def drop_schema = {
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

  def list_schema = {
    logger.info("========== Listing classes ==========")
    Using(graph_factory.getTx) { graph =>
      classes(graph).filter(k => k.isSubClassOf("V") && k.getName != "V")
        .foreach(k => logger.info(f"Node class ${k.getName}"))
      classes(graph).filter(k => k.isSubClassOf("E") && k.getName != "E")
        .foreach(k => logger.info(f"Edge class ${k.getName}"))
    }.get
  }

  def drop_data = {
    Using(graph_factory.getTx) { graph =>
      graph.traversal().V().drop().iterate()
    }.get
  }

  def import_batch[U](data: Seq[U])(implicit meta_source: RecordWithMeta[U]) = {
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

  def query_table(q: Query): Seq[_] = {
    val gremlin_scala_bc = Query2GremlinCompiler.compile(q)
    val gremlin_str = write(GraphsonEncoder.to_value(gremlin_scala_bc))
    val gremlin_native_bc = GremlinIO.readValue(gremlin_str, classOf[Bytecode])
    println("query_table")
    println(q)
    println(gremlin_str)
    println(gremlin_native_bc)
    Using(graph_factory.getTx) { graph =>
      val g = graph.traversal
      val trav: Traversal.Admin[_, _] = JavaTranslator.of(g).translate(gremlin_native_bc)
      trav.asScala.toSeq
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