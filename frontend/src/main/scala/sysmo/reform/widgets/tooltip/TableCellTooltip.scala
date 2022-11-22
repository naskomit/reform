package sysmo.reform.widgets.tooltip

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.widgets.ReactComponent


object TableCellTooltip extends ReactComponent {

  case class Props()
  case class State()
  type StateUpdater = (State => State) => CallbackTo[Unit]

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      <.div("Hello")
    }

  }

  val component =
    ScalaComponent.builder[Props]("Tooltip")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(): Unmounted = {
    component(Props())
  }
}
