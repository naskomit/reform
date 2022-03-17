package sysmo.coviddata.panels

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.{ApplicationPanel, ReactComponent}

object PlotPanel extends ApplicationPanel {
  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div("This is a plot")
    }
  }


  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("Form1")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(): Scala.Unmounted[Props, State, Backend] = {
    component(Props())
  }
}



