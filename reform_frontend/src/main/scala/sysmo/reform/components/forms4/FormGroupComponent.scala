package sysmo.reform.components.forms4

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.components.forms4.{layouts => L}
import sysmo.reform.shared.data.{form4 => F}

object FormGroupComponent extends ReactComponent {
  import japgolly.scalajs.react._

  case class Props(group: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render_form_element(p: Props, elem: F.FormElement, options: FormRenderingOptions): VdomNode = {
      elem match {
        case x: F.FieldEditor => render_field_editor(p, x)
        case x: F.FormGroup => FormGroupComponent(x, p.data_handler, options)
      }
    }

    def render_field_editor(p: Props, editor: F.FieldEditor): VdomNode = {
      editor match {
        case x: F.StringEditor => editors.StringEditorComponent(x, p.data_handler)
        case x: F.BooleanEditor => editors.BooleanEditorComponent(x, p.data_handler)
        case x: F.FloatEditor => editors.FloatEditorComponent(x, p.data_handler)
        case x: F.IntegerEditor => editors.IntegerEditorComponent(x, p.data_handler)
        case x: F.SelectEditor => editors.SelectEditorComponent(x, p.data_handler)
      }
    }

    def render (p: Props, s: State): VdomElement = {
      val child_options = p.options.update(_.depth := p.options.get(_.depth) + 1)
      val children = p.group.elements
        .filter(elem => elem.show(p.data_handler.context(p.group)) match {
          case Right(x) => x
          case Left(err) => {
            logger.error(err)
            true
          }
        })
        .map(elem => {
          val vdom_element = render_form_element(p, elem, child_options)
          val width = elem match {
            case e: F.FormGroup =>  L.FullWidth
            case e: F.FieldEditor => L.Medium
          }
          L.ChildElement(vdom_element, width)
        })
      val layout = p.options.get(_.form_group_layout)
      <.div(layout.apply(p.group.descr, children, p.options))
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("FormGroup")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(group: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions): Unmounted = {
    component(Props(group, data_handler, options))
  }
}
