package sysmo.reform.components.forms4

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.components.forms4.{layouts => L}
import sysmo.reform.components.{Processing, ReactComponent}
import sysmo.reform.shared.data.form4.{FieldEditor, FormGroup}
import sysmo.reform.shared.data.{form4 => F}
import sysmo.reform.util.log.Logging

case class FormElementRenderer(data_handler: FormDataHandler) extends Logging {
  def render_form_element(elem: F.FormElement, options: FormRenderingOptions): VdomNode = {
    elem match {
      case x: F.FieldEditor => render_field_editor(x)
      case x: F.FormGroup => render_form_group(x, options)
    }
  }

  def render_field_editor(editor: F.FieldEditor): VdomNode = {
    editor match {
      case x: F.StringEditor => editors.StringEditorComponent(x, data_handler)
      case x: F.BooleanEditor => editors.BooleanEditorComponent(x, data_handler)
      case x: F.FloatEditor => editors.FloatEditorComponent(x, data_handler)
      case x: F.IntegerEditor => editors.IntegerEditorComponent(x, data_handler)
      case x: F.SelectEditor => editors.SelectEditorComponent(x, data_handler)
    }
  }

  def render_form_group(group: F.FormGroup, options: FormRenderingOptions): VdomNode = {
    val child_options = options.update(_.depth := options.get(_.depth) + 1)
    val children = group.elements
      .filter(elem => elem.show(data_handler.context(group)) match {
        case Right(x) => x
        case Left(err) => {
          logger.error(err)
          true
        }
      })
      .map(elem => {
        val vdom_element = render_form_element(elem, child_options)
        val width = elem match {
          case e: FormGroup =>  L.FullWidth
          case e: FieldEditor => L.Medium
        }
        L.ChildElement(vdom_element, width)
      })
    options.get(_.form_group_layout).apply(group.descr, children, options)
  }


}


object FormEditorComponent extends ReactComponent {
  import japgolly.scalajs.react._
  case class Props(form: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State(render_ind: Int)
  final class Backend($: BackendScope[Props, State]) {
    def bind(p: Props): Unit  = p.data_handler.bind($)
    def render (p: Props, s: State): VdomElement = {
      val element_renderer = new FormElementRenderer(p.data_handler)
      <.form(^.className:= "form", ^.id:= p.form.path.toString,
        p.data_handler.handler_state match {
          case FormDataHandler.HandlerState.Ready => element_renderer.render_form_group(p.form, p.options)
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
//        f.modState(s => s.copy(
//          data = Some(f.props.data_handler.initial_data)
//        )) >>
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
