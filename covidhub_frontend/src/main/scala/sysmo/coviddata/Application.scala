package sysmo.coviddata


import sysmo.reform.router.PageCollection
import router.{Analysis, HomePage, Plots}
import sysmo.reform.{ApplicationConfiguration, ReactApplication}
import sysmo.reform.components.layouts.IntuitiveLayout
import org.scalajs.dom.Event
import sysmo.reform.services.ServerTableDataSource
import sysmo.reform.shared.data.{TableService, graph => G, table => sdt}
import sysmo.coviddata.shared.data.{CovidDatabaseSchema => CDS}
import sysmo.reform.components.panels.TableGroupPanel
import sysmo.reform.shared.data.table.Schema

object AppConfig extends ApplicationConfiguration {
  def make_table_entry(c: G.VertexClass): sdt.Schema = {
    val vertex_schema = c.target
    G.Schema.table_schema_builder(vertex_schema).build
  }

  val table_schemas: Seq[Schema] = Seq(
    make_table_entry(CDS.SocioDemographic),
    make_table_entry(CDS.Clinical),
    make_table_entry(CDS.ClinicalLab),
    make_table_entry(CDS.Therapy1),
    make_table_entry(CDS.Therapy2),
    make_table_entry(CDS.TherapyLab),
    make_table_entry(CDS.Immunology),
  )

  override val table_source: TableService = new ServerTableDataSource(table_schemas: _*)
}


object Application extends ReactApplication {
  override val react_node = "mainApp"
  override def app_config: ApplicationConfiguration = AppConfig
  override def pages = {

    PageCollection(
      HomePage,
      AppConfig.table_schemas.foldLeft(
        TableGroupPanel.builder("TableData", Some("Таблични Данни"))
      )((bld, table) => bld.add(table))
      .build,
      Plots,
      Analysis
    )
  }
  override val layout = IntuitiveLayout

  override def init_react(e: Event): Unit = {
    sdt.enable_global_manager(true)
    super.init_react(e)
  }

//  MermaidFacades.show()

}
