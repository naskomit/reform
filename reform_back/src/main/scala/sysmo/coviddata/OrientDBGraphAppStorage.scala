package sysmo.coviddata

import org.apache.tinkerpop.gremlin.jsr223.JavaTranslator
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory
import org.apache.tinkerpop.gremlin.process.traversal.{Bytecode, P, Traversal, Order}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.process.traversal.step.util.WithOptions
import sysmo.coviddata.shared.data.PatientRecord
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.db.GremlinIO.{readValue, writeValueAsString}
import sysmo.reform.shared.data.RecordWithMeta
import sysmo.reform.shared.query.Query2GremlinCompiler
import scala.jdk.CollectionConverters._

object OrientDBGraphAppStorage {
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
      .and(
        __.has("age", P.gt(35.0)),
        __.has("gender", P.`eq`("жена"))
      )
      //.valueMap().`with`(WithOptions.tokens, WithOptions.all)
      .order().by("age", Order.desc)
      .range(1, 2)

      .valueMap()

    t2.asScala.foreach(println)

    val bc = t2.asAdmin().getBytecode
    val bc_out = writeValueAsString(bc)

    println(bc)
    println(bc_out)
//    t2.asScala.foreach(println)
//
//    val bc = t2.asAdmin().getBytecode
//    val bc_out = writeValueAsString(bc)
//    val bc_in = readValue(bc_out, classOf[Bytecode])
//    //    println(bc)
//    //    println(bc_in)
//
//    println("============================== Reference ==============================")
//    println(bc_out)
//    println(bc_in)
//    import sysmo.reform.shared.{gremlin => smo_gr}
//    println("============================== Test Serialization ==============================")
//    val remote_bytecode_1 = smo_gr.Bytecode.prog1
//    val remote_graphson_1 = smo_gr.Bytecode.test_serialization(remote_bytecode_1)
//    val local_bytecode_1 = readValue(remote_graphson_1, classOf[Bytecode])
//    println(remote_graphson_1)
//    println(local_bytecode_1)
//
//    val trav: Traversal.Admin[_, _] = JavaTranslator.of(g).translate(local_bytecode_1)
//    trav.asScala.foreach(println)
//
//    println("============================== Test Compilation from Query ==============================")
//    val remote_bytecode_2 = Query2GremlinCompiler.test1()
//    val remote_graphson_2 = smo_gr.Bytecode.test_serialization(remote_bytecode_2)
//    val local_bytecode_2 = readValue(remote_graphson_2, classOf[Bytecode])
//    println(remote_graphson_2)
//    println(local_bytecode_2)
//    val trav2: Traversal.Admin[_, _] = JavaTranslator.of(g).translate(local_bytecode_1)
//    trav2.asScala.foreach(println)
//
//
//    println(remote_bytecode_1)
//
//    println(remote_bytecode_2)
//
    graph.close()
  }
}
