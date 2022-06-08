package sysmo.reform.components.forms4

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.components.forms4.{layouts => L}
import sysmo.reform.shared.form4.{BooleanEditor, FieldEditor, FloatEditor, FormElement, FormGroup, GroupArray, IntegerEditor, SelectEditor, StringEditor}
import sysmo.reform.shared.{form4 => F}

object FormGroupComponent extends ReactComponent {
  import japgolly.scalajs.react._

  case class Props(group: FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render_form_element(p: Props, elem: FormElement, options: FormRenderingOptions): VdomNode = {
      elem match {
        case x: FieldEditor => render_field_editor(p, x)
        case x: FormGroup => FormGroupComponent(x, p.data_handler, options)
        case x: GroupArray => GroupArrayComponent(x, p.data_handler, options)
      }
    }

    def render_field_editor(p: Props, editor: FieldEditor): VdomNode = {
      editor match {
        case x: StringEditor => editors.StringEditorComponent(x, p.data_handler)
        case x: BooleanEditor => editors.BooleanEditorComponent(x, p.data_handler)
        case x: FloatEditor => editors.FloatEditorComponent(x, p.data_handler)
        case x: IntegerEditor => editors.IntegerEditorComponent(x, p.data_handler)
        case x: SelectEditor => editors.SelectEditorComponent(x, p.data_handler)
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
            case e: FormGroup =>  L.FullWidth
            case e: FieldEditor => L.Medium
            case e: GroupArray => L.FullWidth
          }
          L.GroupChildElement(vdom_element, width)
        })
      val layout = p.options.get(_.form_group_layout)
      <.div(layout(p.group.descr, children, p.options))
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("FormGroup")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(group: FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions): Unmounted = {
    component(Props(group, data_handler, options))
  }
}
