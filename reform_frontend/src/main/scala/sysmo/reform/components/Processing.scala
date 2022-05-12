package sysmo.reform.components

import japgolly.scalajs.react.vdom.html_<^._

object Processing extends ReactComponent {
  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(^.className := "loading", "Loading&#8230;")
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("Processing")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(): Unmounted = {
    component(Props())
  }
}
