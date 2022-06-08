package sysmo.reform.components

import japgolly.scalajs.react.vdom.html_<^._

object ComponentTemplate extends ReactComponent {
  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div("This is a template component")
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("TemplateComponent")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(): Unmounted = {
    component(Props())
  }
}
