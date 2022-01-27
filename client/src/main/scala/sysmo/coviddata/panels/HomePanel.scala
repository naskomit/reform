package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.coviddata.components.Counter
import sysmo.coviddata.components.table.{AgGridComponent, DemoDataSource}

object HomePanel {
  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      AgGridComponent(DemoDataSource)
    }
  }

  val component = ScalaComponent.builder[Props]("HomePage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}
