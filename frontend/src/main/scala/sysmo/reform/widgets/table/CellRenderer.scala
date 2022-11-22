package sysmo.reform.widgets.table

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.shared.data.Value
import Value.implicits._
import sysmo.reform.effects.ActionManager
import sysmo.reform.widgets.ReactComponent
import sysmo.reform.widgets.tooltip.Tooltip

class CellRenderer(formatter: CellFormatter, actions: CellActions) extends ReactComponent {
  case class Props(v: Value)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      Tooltip()(
        <.div(
          formatter.render(p.v)
        ),
        <.div("Tooltip")
      )
    }

//    def on_click(p: Props, panel_manager: PanelManager)(e: ReactEvent): Callback = Callback {
//      panel_manager.toast(s"Hello from Cell ${p.v.get[String].get}")
//    }
  }
  val component: Scala.Component[Props, State, Backend, CtorType.Props] =
    ScalaComponent.builder[Props]("CellRenderer")
      .initialState(State())
      .renderBackend[Backend]
      .build

  //
  def apply(v: Value): Unmounted = {
    component(Props(v))
  }

//  type jsProps = sysmo.reform.react.table.aggrid.AgGridFacades.ICellRendererParams
//  val jsComponent = ScalaComponent.builder[jsProps]
//    .render_P { props =>
//      apply(props.value)
//    }
//    .build
//    .toJsComponent
}

object CellRenderer {
  def apply(formatter: CellFormatter, actions: CellActions): CellRenderer =
    new CellRenderer(formatter, actions)
}






