package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.table.RecordTableViewer
import sysmo.reform.shared.data.{graph => G}
import sysmo.coviddata.shared.{data => CD}
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.services.ServerTableDataSource
import sysmo.reform.shared.{query => Q}

object HomePanel extends ApplicationPanel {

  import japgolly.scalajs.react._

  case class Props()

  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      //RecordTableViewer[PatientRecord](DemoServerDataSource, "PatientRecord")
      val schema = G.Schema
        .table_schema_builder(CD.Clinical.schema)
        .build
      <.div(
        <.div(^.cls:= "page-title",
          <.h1("COVID-19 Project")
        ),
        <.div(^.cls:= "wrapper wrapper-white",
          "Description"
        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("HomePage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}