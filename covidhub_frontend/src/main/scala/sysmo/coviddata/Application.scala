package sysmo.coviddata


import sysmo.reform.router.{PageCollection, SimplePage}
import router.{HomePage, Plots}
import sysmo.reform.{ApplicationConfiguration, ReactApplication}
import sysmo.reform.components.layouts.IntuitiveLayout
import org.scalajs.dom.Event
import sysmo.coviddata.panels.FormPanel
import sysmo.reform.services.ServerTableDataSource
import sysmo.reform.shared.data.{TableService, graph => G, table => sdt}
import sysmo.coviddata.shared.data.{CovidDatabaseSchema => CDS}
import sysmo.reform.components.panels.TableGroupPanel
import sysmo.reform.shared.data.table.Schema
import sysmo.coviddata.{panels => P}
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.components.forms.TreeBrowser
import sysmo.reform.shared.form.{examples => Ex}

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
      SimplePage("TaskManagementPanel",
        Some("Task Management Panel"), "fa fa-list-check",
        new FormPanel{lazy val fmb = Ex.TaskManagement.builder}
      ),
      SimplePage(
        "CompanyPanel", Some("Company Panel"), "fa fa-building",
        new FormPanel{lazy val fmb = Ex.Company.builder}
      ),
      SimplePage(
        "ReactionKineticsPanel", Some("Reaction Kinetics"), "fa fa-building",
        new FormPanel{lazy val fmb = Ex.ReactionKinetics.builder}
      ),
      SimplePage(
        "BiomarkerAnalyticsPanel", Some("Biomarker Analytics"), "fa fa-book-medical",
        new FormPanel{lazy val fmb = Ex.BiomarkerAnalytics.builder}
      ),
      SimplePage(
        "TreeViewerPanel", Some("TreeViewer"), "fa fa-folder-tree",
        new ApplicationPanel {
          import japgolly.scalajs.react.{BackendScope, ScalaComponent}
          import japgolly.scalajs.react.vdom.VdomElement
          import japgolly.scalajs.react._
          import japgolly.scalajs.react.vdom.html_<^._
          import sysmo.reform.shared.form.{runtime => FR}
          import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._

          case class Props(data: FR.Group)
          type State = Unit
          type Backend = Unit

          def render(p: Props): VdomElement = {
            TreeBrowser(p.data.runtime, p.data.id)
          }
          val component = ScalaComponent.builder[Props]("TreeViewerPanel")
            .render_P(render)
            .build

          override def apply(app_config: ApplicationConfiguration): Unmounted = {
            val root_group = Ex.ReactionKinetics.builder.build()
            component(Props(root_group))
          }
        }
      )
    )
  }
  override val layout = IntuitiveLayout

  override def init_react(e: Event): Unit = {
    sdt.enable_global_manager(true)
    super.init_react(e)
  }
}
