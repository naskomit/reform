package sysmo.reform.components.forms4

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.{Processing, ReactComponent}
import sysmo.reform.shared.data.{form4 => F}

case class FormElementRenderer(data_handler: FormDataHandler) {
  def render_form_element(elem: F.FormElement): VdomElement = {
    elem match {
      case x: F.FieldEditor => render_field_editor(x)
      case x: F.FormGroup => render_form_group(x)
    }
  }

  def render_field_editor(editor: F.FieldEditor): VdomElement = {
    editor match {
      case x: F.StringEditor => editors.StringEditorComponent(x, data_handler)
      case x: F.BooleanEditor => editors.BooleanEditorComponent(x, data_handler)
      case x: F.FloatEditor => editors.FloatEditorComponent(x, data_handler)
      case x: F.IntegerEditor => editors.IntegerEditorComponent(x, data_handler)
      case x: F.SelectEditor => editors.SelectEditorComponent(x, data_handler)
    }
  }

  def render_form_group(group: F.FormGroup): VdomElement = {
    <.div(
      ^.className:="page-subtitle",
      <.h3(group.descr),
      group.elements.map(elem => render_form_element(elem)).toTagMod
    )
  }


}


object FormEditorComponent extends ReactComponent {
  import japgolly.scalajs.react._
  case class Props(form: F.FormGroup, data_handler: FormDataHandler)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def bind(p: Props): Unit  = p.data_handler.bind($)
    def render (p: Props, s: State): VdomElement = {
      val element_renderer = new FormElementRenderer(p.data_handler)
      <.form(^.className:= "form", ^.id:= p.form.path.toString,
        <.div(^.className:= "page-title",
          <.h1(p.form.descr)
        ),
        p.data_handler.state match {
          case FormDataHandler.State.Ready => p.form.elements
            .map(elem => element_renderer.render_form_element(elem))
            .toTagMod
          case FormDataHandler.State.Loading => Processing()
        }
      )
    }


  }

  val component =
    ScalaComponent.builder[Props]("FormEditorComponent")
      .initialState(State())
      .renderBackend[Backend]
      .componentDidMount(f => {
//        f.modState(s => s.copy(
//          data = Some(f.props.data_handler.initial_data)
//        )) >>
        Callback {
          f.backend.bind(f.props)
        }
      })
      .build

  def apply(form: F.FormGroup, data_handler: FormDataHandler): Unmounted = {

    component(Props(form, data_handler))
  }
}
