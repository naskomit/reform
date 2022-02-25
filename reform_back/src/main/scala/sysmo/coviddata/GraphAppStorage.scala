package sysmo.coviddata

import com.orientechnologies.orient.client.remote.ORemoteConnectionManager
import com.orientechnologies.orient.core.metadata.schema.OType
import org.apache.tinkerpop.gremlin.jsr223.JavaTranslator
import org.apache.tinkerpop.gremlin.orientdb.{OrientGraph, OrientGraphFactory}
import org.apache.tinkerpop.gremlin.process.traversal.step.util.WithOptions
import org.apache.tinkerpop.gremlin.structure.T
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{GraphTraversal, __}
import org.apache.tinkerpop.gremlin.process.traversal.{Bytecode, P, Traversal}
import sysmo.coviddata.GraphAppStorage.factory
import sysmo.coviddata.shared.data.PatientRecord
import sysmo.reform.shared.data.{RecordMeta, RecordWithMeta}
import sysmo.reform.shared.query.Query2GremlinCompiler
import sysmo.reform.util.Logging

import scala.collection.mutable
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

    val graph = factory.getNoTx
    data.foreach(row => {
      val prop_args = meta.field_keys.foldLeft(prop_args_init)((acc, item) =>
        acc ++ Seq(meta.fields(item).name, meta.get_value(row, item))
      )

      graph.addVertex(prop_args: _*)
    })
    graph.close()
  }

}

object GraphAppStorage {
  val uri: String = "remote:localhost/covid"
  val factory = new OrientGraphFactory(uri, "nasko", "nasko")

  val app_storage = new GraphAppStorage(factory)

  def test_import()  = {
    val patient_data = CSVDataSource.read_patient_data()
    val patient_record_meta = implicitly[RecordWithMeta[PatientRecord]]._meta
    app_storage.drop_schema
    app_storage.create_schema(Seq(patient_record_meta))
    app_storage.list_schema
    app_storage.drop_data
    app_storage.import_batch(patient_data)
    query_data()
  }

  def query_data() = {
    val graph = factory.getTx
    val g = graph.traversal()
    println("============= All data ============= ")
    g.V()
//      .by(__.unfold())
      .valueMap().`with`(WithOptions.tokens, WithOptions.all)
      .asScala.foreach(println)
    println("============= Filtered data ============= ")
    val t2 = g.V()
      .hasLabel("PatientRecord")
      .filter(__.and(
        __.has("age", P.lt(35.0)),
        __.has("gender", P.`eq`("жена"))
      ))
      .valueMap().`with`(WithOptions.tokens, WithOptions.all)

    t2.asScala.foreach(println)

    val bc = t2.asAdmin().getBytecode
    val bc_out = writeValueAsString(bc)
    val bc_in = readValue(bc_out, classOf[Bytecode])
//    println(bc)
//    println(bc_in)

    println("============================== Reference ==============================")
    println(bc_out)
    println(bc_in)
    import sysmo.reform.shared.{gremlin => smo_gr}
    println("============================== Test Serialization ==============================")
    val remote_bytecode_1 = smo_gr.Bytecode.prog1
    val remote_graphson_1 = smo_gr.Bytecode.test_serialization(remote_bytecode_1)
    val local_bytecode_1 = readValue(remote_graphson_1, classOf[Bytecode])
    println(remote_graphson_1)
    println(local_bytecode_1)

    val trav: Traversal.Admin[_, _] = JavaTranslator.of(g).translate(local_bytecode_1)
    trav.asScala.foreach(println)

    println("============================== Test Compilation from Query ==============================")
    val remote_bytecode_2 = Query2GremlinCompiler.test1()
    val remote_graphson_2 = smo_gr.Bytecode.test_serialization(remote_bytecode_2)
    val local_bytecode_2 = readValue(remote_graphson_2, classOf[Bytecode])
    println(remote_graphson_2)
    println(local_bytecode_2)
    val trav2: Traversal.Admin[_, _] = JavaTranslator.of(g).translate(local_bytecode_1)
    trav2.asScala.foreach(println)


    println(remote_bytecode_1)

    println(remote_bytecode_2)

    graph.close()
  }


  import java.io.ByteArrayInputStream
  import java.io.ByteArrayOutputStream
  import java.io.IOException

  import org.apache.tinkerpop.gremlin.structure.io.graphson
  val GRAPHSON_READER_3_0 = graphson.GraphSONReader.build().create()
  val GRAPHSON_WRITER_3_0 = graphson.GraphSONWriter.build().create()
  def readValue[T](value: String, clazz: Class[_ <: T]): T = try {
    val in = new ByteArrayInputStream(value.getBytes("UTF-8"))
    try
      GRAPHSON_READER_3_0.readObject(in, clazz)
    catch {
      case e: IOException =>
        throw new RuntimeException(e)
    } finally if (in != null) in.close()
  }

  def writeValueAsString(value: Any): String = try {
    val out = new ByteArrayOutputStream
    try {
      GRAPHSON_WRITER_3_0.writeObject(out, value)
      out.toString("UTF-8")
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    } finally if (out != null) out.close()
  }
}