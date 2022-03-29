package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.components.table.RecordTableViewer
import sysmo.reform.shared.{query => Q}
import sysmo.reform.shared.data.{TableDatasource, graph => G}
import sysmo.coviddata.shared.{data => CD}
import sysmo.reform.ApplicationConfiguration

object ClinicalDataPanel extends ApplicationPanel {

  import japgolly.scalajs.react._

  case class Props(table_data_source: TableDatasource)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      //RecordTableViewer[PatientRecord](DemoServerDataSource, "PatientRecord")
      val schema = G.Schema
        .table_schema_builder(CD.Clinical.schema)
        .build
      <.div(
        <.div(^.cls:= "page-title",
          <.h1("Clinical Data")
        ),
        <.div(^.cls:= "wrapper wrapper-white",
          RecordTableViewer(p.table_data_source, schema, Q.SingleTable(CD.Clinical.schema.name))
        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("HomePage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(app_config: ApplicationConfiguration) =
    component(Props(app_config.table_source))
}