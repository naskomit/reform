package sysmo.reform.react

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Component
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

import scala.scalajs.js

object Tooltip extends ReactComponentChildren {

  case class Props()
  case class State(hover: Boolean)

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, children: PropsChildren, s: State): VdomElement = {
      children.toList match {
        case Seq(content: facade.React.Node, tooltip: facade.React.Node, _*) =>
          <.div(
            <.div(
              ^.onMouseEnter --> on_mouse_enter ,
              ^.onMouseLeave --> on_mouse_leave,
              content
            ),
            <.div(^.position:= "fixed", ^.top:= 100.px, ^.left:= 100.px, ^.zIndex:= "1000", tooltip).when(s.hover)
          )
        case _ => <.div("???")
      }
    }

    def on_mouse_enter: Callback = {
      $.modState(s => s.copy(hover = true))
    }

    def on_mouse_leave: Callback =
      $.modState(s => s.copy(hover = false))
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("Tooltip")
      .initialState(State(false))
      .renderBackendWithChildren[Backend]
      .build

  //
  def apply()(content: ChildArg, tooltip: ChildArg): Unmounted = {
    component(Props())(content, tooltip)
  }
}

