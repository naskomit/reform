package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.coviddata.components.Counter

object HomePanel {
  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div("Home", Counter())
    }
  }

  val component = ScalaComponent.builder[Props]("HomePage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}
