package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.coviddata.components.table.{AgGridColumn, AgGridComponent, DemoDataSource, DemoServerDataSource, RecordTableViewer}

import scala.scalajs.js
import sysmo.coviddata.shared.data





object HomePanel {

  import japgolly.scalajs.react._

  case class Props()

  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      RecordTableViewer(DemoServerDataSource)
    }
  }

  val component = ScalaComponent.builder[Props]("HomePage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}