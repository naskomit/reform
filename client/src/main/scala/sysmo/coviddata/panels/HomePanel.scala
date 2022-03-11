package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.table.RecordTableViewer
import sysmo.coviddata.data.DemoServerDataSource
import sysmo.coviddata.shared.data.PatientRecord

object HomePanel {

  import japgolly.scalajs.react._

  case class Props()

  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      RecordTableViewer[PatientRecord](DemoServerDataSource, "PatientRecord")
    }
  }

  val component = ScalaComponent.builder[Props]("HomePage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}