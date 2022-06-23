package sysmo.reform.components.forms

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import sysmo.reform.components.graph.GraphTextualBrowser
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.menu.ButtonToolbar
import sysmo.reform.components.{Processing, ReactComponent}
import sysmo.reform.shared.form.{runtime => FR}
import japgolly.scalajs.react._
object FormEditorComponent extends ReactComponent {
  trait EditorMode
  case object Editor extends EditorMode
  case object TypeBrowser extends EditorMode
  case object RuntimeBrowser extends EditorMode

  case class Props(group: FR.Group, options: FormRenderingOptions)
  case class State(render_ind: Int, mode: EditorMode, width: Double, height: Double)

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(^.className:= "form",
        <.div(^.height:= (s.height - 200).px, ^.overflow:="auto",
          s.mode match {
            case Editor => {
              FormGroupComponent(p.group, p.options)
            }
            case TypeBrowser => {
              GraphTextualBrowser(p.group.runtime.type_graph)
            }
            case RuntimeBrowser => {
              RuntimeTextualBrowser(p.group.runtime)
            }
          },
        ),
        ButtonToolbar.builder
          .button("Ok", Effects.submit())
          .button("Cancel", Effects.cancel())
          .button("Editor", ($.modState(s => s.copy(mode = Editor))).asAsyncCallback)
          .button("Type browser", ($.modState(s => s.copy(mode = TypeBrowser))).asAsyncCallback)
          .button("Runtime browser", ($.modState(s => s.copy(mode = RuntimeBrowser))).asAsyncCallback)
          .build
      )
    }

    object Effects {
      def submit(): AsyncCallback[Unit] = Callback {

      }.asAsyncCallback
      def cancel(): AsyncCallback[Unit] = Callback {

      }.asAsyncCallback
    }


  }

  val component =
    ScalaComponent.builder[Props]("FormEditorComponent")
      .initialState(State(0, Editor, dom.window.innerWidth, dom.window.innerHeight))
      .renderBackend[Backend]
      .componentDidMount(f => {
        Callback {
          f.props.group.runtime.bind {() =>
            f.modState(s => s.copy(render_ind = s.render_ind + 1)).runNow()
          }
        }
      })
      .componentWillUnmount(f => {
        Callback {
          f.props.group.runtime.unbind()
        }
      })
      .build

  def apply(group: FR.Group, options: FormRenderingOptions): Unmounted = {
    component(Props(group, options))
  }
  def apply(group: FR.Group): Unmounted = {
    apply(group, FormRenderingOptions.default)
  }
}
