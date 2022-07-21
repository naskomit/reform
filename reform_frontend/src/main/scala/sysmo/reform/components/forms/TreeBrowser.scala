package sysmo.reform.components.forms

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.{tree => T}
import sysmo.reform.components.tree_nav.TreeNavigatorComponent


object TreeBrowser extends ReactComponent {
  case class State(render_id: Int)
  case class Props(tree: T.Tree[_])

  final class Backend($: BackendScope[Props, State]) {


    def render (p: Props, s: State): VdomElement = {
      <.div(^.cls:= "page-title",
        <.h1("Tree Form Editor"),
        <.div(^.cls:= "row",
          <.div(^.cls:= "col-md-4",
            <.div(^.cls:= "panel panel-default",
              TreeNavigatorComponent(p.tree)
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
    .initialState(State(0))
    .renderBackend[Backend]
    .componentDidMount(f => Callback {
      f.props.tree.renderer = Some(new T.Renderer {
        override def rerender(): Unit = {
          f.modState(s => s.copy(render_id = s.render_id + 1)).runNow()
        }
      })
    })
    .build

  def apply(): Unmounted = component(Props(T.MTree.example1))
}
