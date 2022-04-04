package sysmo.coviddata


import router.Pages
import sysmo.reform.{ApplicationConfiguration, ReactApplication}
import sysmo.reform.components.layouts.IntuitiveLayout
import org.scalajs.dom.Event
import sysmo.reform.services.ServerTableDataSource
import sysmo.reform.shared.data.{TableService, graph => G, table => sdt}
import sysmo.reform.shared.util.{Named, NamedValue}
import sysmo.coviddata.shared.{data => CD}
import sysmo.reform.components.diagrams.mermaid.MermaidFacades

object AppConfig extends ApplicationConfiguration {
  override val table_source: TableService = new ServerTableDataSource(
    NamedValue(
      "SocioDemographic", Some("Социо-демографски"),
      G.Schema.table_schema_builder(CD.SocioDemographic.schema).build
    ),
    NamedValue("Clinical", Some("Клинични"),
      G.Schema.table_schema_builder(CD.Clinical.schema).build
    ),
    NamedValue("Therapy", Some("Терапия"),
      G.Schema.table_schema_builder(CD.Therapy.schema).build
    ),
    NamedValue("Immunology", Some("Имунология"),
      G.Schema.table_schema_builder(CD.Immunology.schema).build
    ),
  )
}


object Application extends ReactApplication {
  override val react_node = "mainApp"
  override val pages = Pages.collection
  override val app_config: ApplicationConfiguration = AppConfig
  override val layout = IntuitiveLayout

  override def init_react(e: Event): Unit = {
    sdt.enable_global_manager(true)
    super.init_react(e)
  }

//  MermaidFacades.show()

}
