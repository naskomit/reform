package sysmo.coviddata.components

import japgolly.scalajs.react.callback.{Callback, CallbackTo}
import japgolly.scalajs.react.vdom.html_<^._

object Counter {
  import japgolly.scalajs.react.component.Scala.BackendScope
  import japgolly.scalajs.react.ScalaComponent


  case class Props()
  case class State(i : Int = 0)

  final class Backend($: BackendScope[Props, State]) {
    def inc : Callback = $.modState(s => State(s.i + 1))

    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.button(^.onClick --> inc, "Inc"),
        <.div(s.i)
      )
    }
  }

  val component = ScalaComponent.builder[Props]("Counter")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}
