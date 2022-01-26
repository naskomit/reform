package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.html_<^._

object Form1Panel {
  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div("Form1")
    }
  }

  val component = ScalaComponent.builder[Props]("Form1")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}
