package sysmo.reform.components.forms

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.property.PropertyEditor
import sysmo.reform.shared.{tree => T}
import sysmo.reform.components.tree_nav.TreeNavigatorComponent
import sysmo.reform.shared.form.{runtime => FR}

object TreeBrowser extends ReactComponent {
  case class State(render_id: Int, active_item: Option[FR.ObjectId])
  case class Props(runtime: FR.FormRuntime, id: FR.ObjectId, tree: Option[FR.Tree])

  final class Backend($: BackendScope[Props, State]) {


    def render (p: Props, s: State): VdomElement = {
      <.div(^.cls:= "page-title",
        <.h1("Tree Form Editor"),
        p.tree match {
          case Some(tree) => <.div(^.cls:= "row",
            <.div(^.cls:= "col-md-4",
              <.div(^.cls:= "panel panel-default",
                TreeNavigatorComponent(tree)
              ),
            ),
            <.div(^.cls:= "col-md-8",
              <.div(^.cls:= "wrapper wrapper-white",
                s.active_item.flatMap(id => p.runtime.get(id)) match {
                  case Some(group: FR.Group) => PropertyEditor(new FR.PropertyView.GroupAsPropertySource(group))
                  case Some(array: FR.Array) => ArrayBrowser(array, FormRenderingOptions.default)
                  case _ => "Nothing to show"
                }
              ),
            ),
          )

          case None => <.div("Nothing to display")
        }

      )
    }

  }

  val component =
    ScalaComponent.builder[Props]("TreeBrowser")
    .initialState(State(0, None))
    .renderBackend[Backend]
    .componentDidMount(f => Callback {
      f.props.tree.foreach(tree => tree.renderer = Some(new T.Renderer {
        override def rerender(): Unit = {
          val runtime = f.props.runtime
          f.modState(s => s.copy(
            render_id = s.render_id + 1,
            active_item = tree.selection.headOption
          )).runNow()
        }
      }))
    })
    .build

  def apply(runtime: FR.FormRuntime, id: FR.ObjectId): Unmounted = {
    import sysmo.reform.shared.form.runtime.TreeView._
    val tree: Option[FR.Tree] = runtime.get(id).map(x => x)
    component(Props(runtime, id, tree))
  }
}
