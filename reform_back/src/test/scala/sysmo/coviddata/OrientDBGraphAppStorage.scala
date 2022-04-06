package sysmo.coviddata

import scala.util.Using
import scala.jdk.CollectionConverters._
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.process.traversal.step.util.WithOptions
import org.apache.tinkerpop.gremlin.process.traversal.{Order, P}
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.data.{table => dt}
import sysmo.reform.shared.{query => Q}
import sdt.{VectorType => VT}
import sysmo.reform.shared.util.pprint._
import sdt.Printers._
import sysmo.coviddata.shared.{data => CD}
import sysmo.coviddata.io.ExcelImporter
import sysmo.coviddata.shared.data.CovidDatabaseSchema
import sysmo.reform.io.excel.{TableCollectionRead, WorkbookReader}
import sysmo.reform.shared.data.form.RecordWithMeta
import sysmo.reform.util.Logging

object OrientDBGraphAppStorage extends Logging {
  val uri: String = "remote:localhost/covid"
  val factory = new OrientGraphFactory(uri, "sysmo", "sysmopass")
  val app_storage = new GraphAppStorage(factory, CovidDatabaseSchema)

  val doc_path = "doc/SampleData_3.xlsx"

  def test_import(): Unit = {
    app_storage.drop_schema
    app_storage.drop_data
    app_storage.apply_schemas()
    sdt.with_table_manager()(tm => {
      val reader = new WorkbookReader(doc_path, tm)
      val data = reader.read_table_collection(TableCollectionRead(Map(
        "SocioDemographic" -> ExcelImporter.read_sociodemographic,
        "Clinical_1" -> ExcelImporter.read_clinical_1,
        "Clinical_2" -> ExcelImporter.read_clinical_2,
        "Clinical_4" -> ExcelImporter.read_clinical_4,
        "Therapy 2" -> ExcelImporter.read_therapy_2,
        "Immunology" -> ExcelImporter.read_immunology
      )))

//      pprint(data("Immunology"))
//      println(data("Clinical_1").column("14a").iterator.foreach(x => println(x.as_real, x.as_date)))
      logger.info("Excel data read")
      app_storage.upsurt_vertices(CD.SocioDemographic.schema, data("SocioDemographic"), "1")
      app_storage.upsurt_vertices(CD.Clinical.schema, data("Clinical_1"), "1")
      app_storage.upsurt_vertices(CD.Clinical.schema, data("Clinical_2"), "1")
      app_storage.upsurt_vertices(CD.Clinical.schema, data("Clinical_4"), "1")
      app_storage.upsurt_vertices(CD.Therapy.schema, data("Therapy 2"), "1")
      app_storage.upsurt_vertices(CD.Immunology.schema, data("Immunology"), "1")
      logger.info("Data imported into the database")
    })
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
