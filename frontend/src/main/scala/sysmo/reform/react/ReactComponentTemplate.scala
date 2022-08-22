package sysmo.reform.react

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object ReactComponentTemplate extends ReactComponent {

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div("This is a template component")
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("ReactComponentTemplate")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(): Unmounted = {
    component(Props())
  }
}
