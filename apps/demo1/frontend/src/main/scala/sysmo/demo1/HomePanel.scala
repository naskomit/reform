package sysmo.demo1

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.app.{Configuration, Panel}

object HomePanel extends Panel {

  import japgolly.scalajs.react._

  case class Props()

  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.cls:= "page-title",
          <.h1("Reform Demo 1")
        ),
        <.div(^.cls:= "wrapper wrapper-white",
          "Description"
        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("HomePage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(app_config: Configuration) = component(Props())
}