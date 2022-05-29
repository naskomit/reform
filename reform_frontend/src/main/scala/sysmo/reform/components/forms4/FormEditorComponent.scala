package sysmo.reform.components.forms4

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.components.{Processing, ReactComponent}
import sysmo.reform.shared.data.{form4 => F}

object FormEditorComponent extends ReactComponent {
  import japgolly.scalajs.react._
  case class Props(form: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State(render_ind: Int)
  final class Backend($: BackendScope[Props, State]) {
    def bind(p: Props): Unit  = p.data_handler.bind($)
    def render (p: Props, s: State): VdomElement = {
//      val element_renderer = new FormElementRenderer(p.data_handler)
      <.form(^.className:= "form", ^.id:= p.form.path.toString,
        p.data_handler.handler_state match {
          case FormDataHandler.HandlerState.Ready => FormGroupComponent(p.form, p.data_handler, p.options)
          case FormDataHandler.HandlerState.Loading => Processing()
        }
      )
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
      .build

  def apply(form: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions): Unmounted = {
    component(Props(form, data_handler, options))
  }
  def apply(form: F.FormGroup, data_handler: FormDataHandler): Unmounted = {
    apply(form, data_handler, FormRenderingOptions.default)
  }
}
