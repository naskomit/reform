package sysmo.coviddata

import scala.util.{Success, Try, Using}
import scala.jdk.CollectionConverters._
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{GraphTraversal, GraphTraversalSource, __}
import org.apache.tinkerpop.gremlin.process.traversal.step.util.WithOptions
import org.apache.tinkerpop.gremlin.process.traversal.{Order, P}
import org.apache.tinkerpop.gremlin.structure.{Edge, T, Vertex}
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.shared.data.{graph => G, table => sdt}
import sysmo.reform.data.{table => dt}
import sysmo.reform.shared.{query => Q}
import sdt.{Row, VectorType => VT}
import sysmo.reform.shared.util.pprint._
import sdt.Printers._
import sysmo.reform.data.graph.CompositeTraversals
import sysmo.coviddata.io.ExcelImporter
import sysmo.coviddata.shared.data.{CovidDatabaseSchema => CDS}
import sysmo.reform.io.excel.{TableCollectionRead, WorkbookReader}
import sysmo.reform.shared.data.form.RecordWithMeta
import sysmo.reform.util.FuncLogging
import sysmo.reform.util.Prog._

object OrientDBGraphAppStorage extends FuncLogging {
  val uri: String = "remote:localhost/covid"
  val factory = new OrientGraphFactory(uri, "sysmo", "sysmopass")
  val app_storage = new GraphAppStorage(factory, CDS)

  val doc_path = "data/raw/SampleData_3.xlsx"
  import sysmo.reform.data.graph.Implicits._

  protected def insert_data_secondary(data: sdt.Table,
                                      new_vertex_schema: G.VertexSchema,
                                      new_edge_schema: G.EdgeSchema): Res[Unit] = {
    app_storage.insert_from_table(data, (tb =>
      tb.start("from",(g, row) =>
        g.V().has(T.label, "SocioDemographic")
          .has("1", row.get("1").v.get)
      ).extend("to", (trav, row) =>
          trav.b_update_or_create_vertex(
            __.V().has(T.label, new_vertex_schema.name).has("1", row.get("1").v.get),
            new_vertex_schema, row
          )
      ).extend("edge", (trav, row) =>
        trav.b_update_or_create_edge(
          __.select("from").outE().has(T.label, new_edge_schema.name).where(__.inV().as("to")),
          new_edge_schema, row, "from", "to"
        )
      )
    ))
  }

  protected def insert_data_multiple(data: sdt.Table,
                                      new_vertex_schema: G.VertexSchema,
                                      new_edge_schema: G.EdgeSchema): Res[Unit] = {
    app_storage.insert_from_table(data, (tb =>
      tb.start("from",(g, row) =>
        g.V().has(T.label, "SocioDemographic")
          .has("1", row.get("1").v.get)
      ).extend("to", (trav, row) =>
        trav.addV(new_vertex_schema.name).b_append_props(new_vertex_schema, row)
//        trav.b_update_or_create_vertex(
//          __.V().has(T.label, new_vertex_schema.name).has("1", row.get("1").v.get),
//          new_vertex_schema, row
//        )
      ).extend("edge", (trav, row) =>
        trav.b_update_or_create_edge(
          __.select("from").outE().has(T.label, new_edge_schema.name).where(__.inV().as("to")),
          new_edge_schema, row, "from", "to"
        )
      )
      ))
  }

  def test_import(): Res[Unit] = {
    app_storage.drop_data().get
    app_storage.drop_schema().get
    app_storage.apply_schemas().get
    sdt.with_table_manager()(tm => {
      val reader = new WorkbookReader(doc_path, tm)
      val data = reader.read_table_collection(TableCollectionRead(Map(
        "SocioDemographic" -> ExcelImporter.read_sociodemographic,
        "Clinical_1" -> ExcelImporter.read_clinical_1,
        "Clinical_2" -> ExcelImporter.read_clinical_2,
        "Clinical_3" -> ExcelImporter.read_clinical_lab,
        "Clinical_4" -> ExcelImporter.read_clinical_4,
        "Therapy_1" -> ExcelImporter.read_therapy_1,
        "Therapy_2" -> ExcelImporter.read_therapy_2,
        "Therapy_Lab" -> ExcelImporter.read_therapy_lab,
        "Immunology" -> ExcelImporter.read_immunology
      )))

      logger.info("Excel data read")
//      val strategy = CreateOrUpdate("1")

//      data("Therapy_Lab").pprint()
      app_storage.insert_vertices(
        data("SocioDemographic"),
        (g, row) => g.b_create_vertex(CDS.SocioDemographic.target, row)
      )

      insert_data_secondary(data("Clinical_1"), CDS.Clinical.target, CDS.HasClinical.target)
      insert_data_secondary(data("Clinical_2"), CDS.Clinical.target, CDS.HasClinical.target)
      insert_data_multiple(data("Clinical_3"), CDS.ClinicalLab.target, CDS.HasClinicalLab.target)
      insert_data_secondary(data("Clinical_4"), CDS.Clinical.target, CDS.HasClinical.target)

      insert_data_multiple(data("Therapy_1"), CDS.Therapy1.target, CDS.HasTherapy1.target)
      insert_data_secondary(data("Therapy_2"), CDS.Therapy2.target, CDS.HasTherapy2.target)
      insert_data_multiple(data("Therapy_Lab"), CDS.TherapyLab.target, CDS.HasTherapyLab.target)
      insert_data_secondary(data("Immunology"), CDS.Immunology.target, CDS.HasImmunology.target)



//      app_storage.insert_linked_vertices(
//        data("Clinical_1"),
//        (trav, row) =>
//          trav.has(T.label, "SocioDemographic")
//          .has("1", row.get("1").v.get),
//        (trav, row) =>
//          trav.b_find_or_create_vertex(
//            __.V().has(T.label, "Clinical").has("1", row.get("1").v.get),
//            __.addV("Clinical").b_append_props(CDS.Clinical.target, row),
//          ),
//        (trav, row) => trav.addE("HasClinical")
//      ).get

//      app_storage.insert_linked_vertices(
//        CDS.HasTherapy.target, to_other = false, data("Therapy 2"), strategy, find_strategy, edge_strategy
//      ).get
//      app_storage.insert_linked_vertices(
//        CDS.HasImmunology.target, to_other = false, data("Immunology"), strategy, find_strategy, edge_strategy
//      ).get
//      app_storage.modify_vertices(CDS.Clinical.schema, data("Clinical_2"), strategy).get
//      app_storage.modify_vertices(CDS.Clinical.schema, data("Clinical_4"), strategy).get
//      app_storage.modify_vertices(CDS.Therapy.schema, data("Therapy 2"), strategy).get
//      app_storage.modify_vertices(CDS.Immunology.schema, data("Immunology"), strategy).get
      logger.info("Data imported into the database")
    })
  }

//  def query_data() = {
//    val graph = factory.getTx
//    val g = graph.traversal()
//    println("============= All data ============= ")
//    g.V()
//      //      .by(__.unfold())
//      .valueMap().`with`(WithOptions.tokens, WithOptions.all)
//      .asScala.foreach(println)
//    println("============= Filtered data ============= ")
//    val t2 = g.V()
//      .hasLabel("PatientRecord")
//      .and(
//        __.has("age", P.gt(35.0)),
//        __.has("gender", P.`eq`("жена"))
//      )
//      //.valueMap().`with`(WithOptions.tokens, WithOptions.all)
//      .order().by("age", Order.desc)
////      .range(1, 2)
//      .valueMap()
//      .select("first_name", "father_name", "last_name", "gender", "age", "education")
////      .by(__.unfold)
//
////    val data_in = t2.asScala.map(_.asInstanceOf[java.util.Map[String, Any]].asScala)
////    println(data_in.toSeq)
//    val schema = sdt.Schema(Seq(
//      sdt.Field("first_name", sdt.FieldType(VT.Char)), sdt.Field("father_name", sdt.FieldType(VT.Char)),
//      sdt.Field("last_name", sdt.FieldType(VT.Char)), sdt.Field("gender", sdt.FieldType(VT.Char)),
//      sdt.Field("age", sdt.FieldType(VT.Int)), sdt.Field("education", sdt.FieldType(VT.Char))
//    ))
//
//
//    Using(dt.arrow.ArrowTableManager()) { tm => {
//      val tb_1 = tm.incremental_table_builder(schema)
//      t2.asScala.foreach(x => {
//        val prop_map = x.asInstanceOf[java.util.Map[String, Any]].asScala.toMap
//            .view.mapValues {
//              case el: java.util.List[_] => Some(el.get(0))
//              case x => Some(x)
//            }.toMap
//
//        tb_1 :+ prop_map
//      })
//      val tbl_1 = tb_1.toTable
//      pprint(tbl_1)
//    }}.get
//
//    println()
//
//    graph.close()
//  }

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
