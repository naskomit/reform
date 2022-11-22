package sysmo.reform.widgets.table

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.component.Scala
import sysmo.reform.shared.data.Value
import Value.implicits._
import sysmo.reform.effects.ActionManager
import sysmo.reform.widgets.ReactComponent
import sysmo.reform.widgets.tooltip.{TooltipController, TooltipViewer}

class CellRenderer(formatter: CellFormatter, actions: CellActions) extends ReactComponent {

  case class Props(v: Value)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      formatter.render(p.v)
    }
  }

  val component: Scala.Component[Props, State, Backend, CtorType.Props] =
    ScalaComponent.builder[Props]("CellRenderer")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(v: Value): Unmounted = {
    component(Props(v))
  }

}

object CellRenderer {
  def apply(formatter: CellFormatter, actions: CellActions): CellRenderer =
    new CellRenderer(formatter, actions)
}






