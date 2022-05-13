package sysmo.reform.components.forms3

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.{Processing, ReactComponent}
import sysmo.reform.shared.data.{form3 => F}
import F.{FormData => FD}
import sysmo.reform.components.forms3.editors.EditorAction

import scala.util.DynamicVariable

case class FormElementRenderer(data_handler: FormDataHandler) {
  def render_form_element(elem: F.FormElement, data: FD.ValueMap): VdomElement = {
    elem match {
      case x: F.FieldEditor => render_field_editor(x, data(elem.path))
      case x: F.FormGroup => render_form_group(x, data)
    }
  }

  def render_field_editor(editor: F.FieldEditor, value: FD.FieldValue[_]): VdomElement = {
    editor match {
      case x: F.StringEditor => editors.StringEditorComponent(x, value.asInstanceOf[FD.CharValue], data_handler)
      case x: F.SelectEditor => editors.SelectEditorComponent(x, value.asInstanceOf[FD.CharValue], data_handler)
      case x: F.BooleanEditor => editors.BooleanEditorComponent(x, value.asInstanceOf[FD.BoolValue], data_handler)
      case x: F.FloatEditor => editors.FloatEditorComponent(x, value.asInstanceOf[FD.FloatValue], data_handler)
      case x: F.IntegerEditor => editors.IntegerEditorComponent(x, value.asInstanceOf[FD.IntValue], data_handler)
    }
  }

  def render_form_group(g: F.FormGroup, data: FD.ValueMap): VdomElement = {
    g match {
      case x: F.FieldSet => render_field_set(x, data)
      case x: F.Section => render_section(x, data)
    }
  }

  def render_field_set(fs: F.FieldSet, data: FD.ValueMap):VdomElement = {
    <.div(
      ^.className:="page-subtitle",
      <.h3(fs.make_label),
      fs.fields.map(field => render_field_editor(field, data(field.path))).toTagMod
    )
  }

  def render_section(sec: F.Section, data: FD.ValueMap):VdomElement = {
    <.div()
  }
}


object FormEditorComponent extends ReactComponent {
  import japgolly.scalajs.react._

  case class Props(form: F.Form, data_handler: FormDataHandler)
  case class State(data: Option[FD.ValueMap])
  final class Backend($: BackendScope[Props, State]) {
    def bind(p: Props): Unit  = p.data_handler.bind($)
    def render (p: Props, s: State): VdomElement = {
      val element_renderer = new FormElementRenderer(p.data_handler)
      <.form(^.className:= "form", ^.id:= p.form.id.toString,
        <.div(^.className:= "page-title",
          <.h1(p.form.make_label)
        ),
        s.data match {
          case Some(data) => p.form.elements.map(elem => element_renderer.render_form_element(elem, data)).toTagMod
          case _ => Processing()
        }
      )
    }


  }

  val component =
    ScalaComponent.builder[Props]("FormEditorComponent")
      .initialState(State(None))
      .renderBackend[Backend]
      .componentDidMount(f => {
        f.modState(s => s.copy(
          data = Some(f.props.data_handler.initial_data)
        )) >> Callback {
          f.backend.bind(f.props)
        }
      })
      .build

  def apply(form: F.Form, data_handler: FormDataHandler): Unmounted = {

    component(Props(form, data_handler))
  }
}
