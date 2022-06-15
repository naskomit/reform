package sysmo.reform.components.forms

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.menu.ButtonToolbar
import sysmo.reform.components.{Processing, ReactComponent}
import sysmo.reform.shared.{form => F}

object FormEditorComponent extends ReactComponent {
  import japgolly.scalajs.react._
  case class Props(form: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State(render_ind: Int)
  final class Backend($: BackendScope[Props, State]) {
    def bind(p: Props): Unit  = p.data_handler.bind($)
    def unbind(p: Props): Unit  = p.data_handler.unbind($)
    def render (p: Props, s: State): VdomElement = {
      <.div(^.className:= "form", ^.id:= p.form.path.toString,
        p.data_handler.handler_state match {
          case FormDataHandler.HandlerState.Ready => FormGroupComponent(p.form, p.data_handler)(p.options)
          case FormDataHandler.HandlerState.Loading => Processing()
        },
        ButtonToolbar.builder
          .button("Ok", Effects.submit())
          .button("Cancel", Effects.cancel())
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
      .initialState(State(0))
      .renderBackend[Backend]
      .componentDidMount(f => {
        Callback {
          f.backend.bind(f.props)
        }
      })
      .componentWillUnmount(f => {
        Callback {
          f.backend.unbind(f.props)
        }
      })
      .build

  def apply(form: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions): Unmounted = {
    component(Props(form, data_handler, options))
  }
  def apply(form: F.FormGroup, data_handler: FormDataHandler): Unmounted = {
    apply(form, data_handler, FormRenderingOptions.default)
  }
}
