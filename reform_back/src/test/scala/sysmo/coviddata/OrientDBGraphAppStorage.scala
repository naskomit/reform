package sysmo.coviddata

import scala.util.Using
import scala.jdk.CollectionConverters._

import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.process.traversal.step.util.WithOptions
import org.apache.tinkerpop.gremlin.process.traversal.{Order, P}

import sysmo.coviddata.shared.data.PatientRecord
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.shared.data.RecordWithMeta
import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.data.{table => dt}
import sysmo.reform.shared.{query => Q}
import sdt.{VectorType => VT}
import sysmo.reform.shared.util.pprint._
import sdt.Printers._

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
      pprint(tbl_1)
    }}.get

    println()

    graph.close()
  }

  def test_query_table(): Unit = {
    Using(dt.arrow.ArrowTableManager()) { tm => {
      val q = Q.BasicQuery(
        source = Q.SingleTable("PatientRecord")
//        filter = None, //Option[QueryFilter]
//        sort = None, //: Option[QuerySort],
//        range = None, //: Option[QueryRange]
      )
      val result = app_storage.query_table(q, tm)
      pprint(result)
    }}.get
  }
}
