package sysmo.reform.components.forms

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.{tree => T}
import sysmo.reform.components.tree_nav.TreeNavigatorComponent


object TreeBrowser extends ReactComponent {
  case class State()
  case class Props()

  final class Backend($: BackendScope[Props, State]) {


    def render (p: Props, s: State): VdomElement = {
      <.div(^.cls:= "page-title",
        <.h1("Tree Form Editor"),
        <.div(^.cls:= "row",
          <.div(^.cls:= "col-md-4",
            <.div(^.cls:= "panel panel-default",
              TreeNavigatorComponent(T.MTree.example1)
            ),
          ),
          <.div(^.cls:= "col-md-8",
            <.div(^.cls:= "panel panel-default",
              "Property Editor"
            ),
          ),
        )
      )
    }

  }

  val component =
    ScalaComponent.builder[Props]("TreeBrowser")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(): Unmounted = component(Props())
}
