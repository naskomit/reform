package sysmo.reform.components.forms3

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.data.{form3 => F}
import F.{FormData => FD}
import sysmo.reform.components.forms3.editors.EditorAction

import scala.util.DynamicVariable

case class FormElementRenderer(form_dispatcher: EditorAction.Dispatcher) {
  def render_form_element(elem: F.FormElement, data: FD.ValueMap): VdomElement = {
    elem match {
      case x: F.FieldEditor => render_field_editor(x, data(elem.path))
      case x: F.FormGroup => render_form_group(x, data)
    }
  }

  def render_field_editor(editor: F.FieldEditor, value: FD.FieldValue[_]): VdomElement = {
    editor match {
      case x: F.StringEditor => editors.StringEditorComponent(x, value.asInstanceOf[FD.CharValue], form_dispatcher)
      case x: F.SelectEditor => editors.SelectEditorComponent(x, value.asInstanceOf[FD.CharValue], form_dispatcher)
      case x: F.BooleanEditor => editors.BooleanEditorComponent(x, value.asInstanceOf[FD.BoolValue], form_dispatcher)
      case x: F.FloatEditor => editors.FloatEditorComponent(x, value.asInstanceOf[FD.FloatValue], form_dispatcher)
      case x: F.IntegerEditor => editors.IntegerEditorComponent(x, value.asInstanceOf[FD.IntValue], form_dispatcher)
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

  case class Props(form: F.Form, data: FD.ValueMap, form_dispatcher: EditorAction.Dispatcher)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val element_renderer = new FormElementRenderer(p.form_dispatcher)
      <.form(^.className:= "form", ^.id:= p.form.id.toString,
        <.div(^.className:= "page-title",
          <.h1(p.form.make_label)
        ),
        p.form.elements.map(elem => element_renderer.render_form_element(elem, p.data)).toTagMod
      )
    }


  }

  val component =
    ScalaComponent.builder[Props]("FormEditorComponent")
      .initialState(State())
      .renderBackend[Backend]
      //      .componentDidMount(f => f.backend.init(f.props))
      .build

  def apply(form: F.Form, data: FD.ValueMap, form_dispatcher: EditorAction.Dispatcher): Unmounted = {

    component(Props(form, data, form_dispatcher))
  }
}
