package sysmo.coviddata.components.editors

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object StringEditor extends AbstractEditor {
  case class Props(value : String)
  case class State(value: String)

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(
        <.input(^.`type` := "text", ^.value := s.value, ^.onChange ==> on_value_change,
          ^.onBlur --> on_blur
      ))
    }

    def on_value_change(event : ReactEventFromInput) =
      $.modState(_ => State(event.target.value)) >>
      Callback {
        println(s"Changed to ${event.target.value}")
      }
    }

    def on_blur = Callback {
      println("Lost focus")
    }


  val component = ScalaComponent.builder[Props]("Form1")
    .initialState(State("hello-state"))
    .renderBackend[Backend]
    .build

  def apply(value : String) = component(Props(value))

}
