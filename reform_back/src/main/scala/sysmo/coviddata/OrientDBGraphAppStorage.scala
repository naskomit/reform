package sysmo.coviddata

import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory
import org.apache.tinkerpop.gremlin.process.traversal.{Bytecode, Order, P, Traversal}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.process.traversal.step.util.WithOptions
import sysmo.coviddata.shared.data.PatientRecord
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.shared.data.RecordWithMeta
import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.data.{table => dt}

import scala.jdk.CollectionConverters._
import scala.util.Using

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
//      .range(1, 2)
      .valueMap()
      .select("first_name", "father_name", "last_name", "gender", "age", "education")
//      .by(__.unfold)

//    val data_in = t2.asScala.map(_.asInstanceOf[java.util.Map[String, Any]].asScala)
//    println(data_in.toSeq)

    import sdt.{VectorType => VT}
    val schema = sdt.Schema(Seq(
      sdt.Field("first_name", sdt.FieldType(VT.Char)), sdt.Field("father_name", sdt.FieldType(VT.Char)),
      sdt.Field("last_name", sdt.FieldType(VT.Char)), sdt.Field("gender", sdt.FieldType(VT.Char)),
      sdt.Field("age", sdt.FieldType(VT.Int)), sdt.Field("education", sdt.FieldType(VT.Char))
    ))

    Using(dt.arrow.ArrowTableManager()) { tm => {
      val tb_1 = tm.incremental_table_builder(schema)
      t2.asScala.foreach(x => {
        val prop_map = x.asInstanceOf[java.util.Map[String, Any]].asScala.toMap
            .view.mapValues {
              case el: java.util.List[_] => Some(el.get(0))
              case x => Some(x)
            }.toMap

        tb_1 :+ prop_map
      })
      val tbl_1 = tb_1.toTable
      println(tbl_1.pprint)
    }}.get

    println()

//      for (x <- t2.iterator().asScala) {
//        println(x)
//      }
//      t2.asScala.foreach(println)

//    val bc = t2.asAdmin().getBytecode
//    val bc_out = writeValueAsString(bc)

//    println(bc)
//    println(bc_out)
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
