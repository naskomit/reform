package sysmo.reform.widgets.tooltip

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import sysmo.reform.effects.{ActionManager, HideTooltip, ShowTooltip}
import sysmo.reform.widgets.ReactComponentChildren

object TooltipController extends ReactComponentChildren {

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, children: PropsChildren, s: State): VdomElement = {
      children.toList match {
        case Seq(content: facade.React.Node, tooltip: facade.React.Node, _*) =>
          ActionManager.ctx.consume(action_manager =>
            <.div(
              ^.onMouseOver ==> on_mouse_over(action_manager, tooltip),
              ^.onMouseOut ==> on_mouse_out(action_manager),
              content
            )
          )
        case _ => <.div("???")
      }
    }

    def on_mouse_over(action_manager: ActionManager, tooltip: facade.React.Node)(e: ReactMouseEvent): Callback =
      action_manager.callback(ShowTooltip(e.clientX, e.clientY, tooltip))

    def on_mouse_out(action_manager: ActionManager)(e: ReactMouseEvent): Callback =
      action_manager.callback(HideTooltip)
    }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("Tooltip")
      .initialState(State())
      .renderBackendWithChildren[Backend]
      .build

  //
  def apply()(content: ChildArg, tooltip: ChildArg): Unmounted = {
    component(Props())(content, tooltip)
  }
}
