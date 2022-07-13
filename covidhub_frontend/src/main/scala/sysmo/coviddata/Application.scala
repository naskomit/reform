package sysmo.coviddata


import sysmo.reform.router.{PageCollection, SimplePage}
import router.{HomePage, Plots}
import sysmo.reform.{ApplicationConfiguration, ReactApplication}
import sysmo.reform.components.layouts.IntuitiveLayout
import org.scalajs.dom.Event
import sysmo.reform.services.ServerTableDataSource
import sysmo.reform.shared.data.{TableService, graph => G, table => sdt}
import sysmo.coviddata.shared.data.{CovidDatabaseSchema => CDS}
import sysmo.reform.components.panels.TableGroupPanel
import sysmo.reform.shared.data.table.Schema
import sysmo.coviddata.{panels => P}

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
//      SimplePage("RefrigerationCycle", Some("Refrigeration cycle"), "fa fa-pencil", P.RefrigerationCyclePanel),
//      SimplePage("BiomarkerAnalysis", Some("Biomarker analysis"), "fa fa-pencil", P.BiomarkerAnalysisPanel),
      SimplePage("TaskManagementPanel", Some("Task Management Panel"), "fa fa-list-check",  P.TaskManagementPanel),
      SimplePage("CompanyPanel", Some("Company Panel"), "fa fa-building",  P.CompanyPanel),
      SimplePage("ReactionKineticsPanel", Some("Reaction Kinetics"), "fa fa-building",  P.ReactionKineticsPanel)
    )
  }
  override val layout = IntuitiveLayout

  override def init_react(e: Event): Unit = {
    sdt.enable_global_manager(true)
    super.init_react(e)
  }
}
