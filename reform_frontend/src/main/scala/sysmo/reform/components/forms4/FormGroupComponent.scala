package sysmo.reform.components.forms4

import japgolly.scalajs.react.component.Js
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.forms4.editors.AbstractFormComponent
import sysmo.reform.components.forms4.layouts.GroupChildNode
import sysmo.reform.components.forms4.options.{FormRenderingOptions, FormRenderingRules}
import sysmo.reform.components.forms4.{layouts => L}
import sysmo.reform.shared.{form => F}

object FormGroupComponent extends AbstractFormComponent {
  import japgolly.scalajs.react._

  case class Props(group: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State()

  final class Backend($: BackendScope[Props, State]) {

    def render_child(element: F.FormElement, p: Props, child_options: FormRenderingOptions): GroupChildNode = {
      val element_fn: FormRenderingOptions => VdomElement = element match {
        case x: F.FieldEditor => render_field_editor(x, p.data_handler, child_options)
        case x: F.FormGroup => FormGroupComponent(x, p.data_handler)
        case x: F.GroupArray => GroupArrayComponent(x, p.data_handler)
      }
      GroupChildNode(element_fn, size_hint(element), child_options)
    }

    def render_field_editor(editor: F.FieldEditor, data_handler: FormDataHandler, child_options: FormRenderingOptions): FormRenderingOptions => VdomElement = {
      editor match {
        case x: F.StringEditor => editors.StringEditorComponent(x, data_handler)
        case x: F.BooleanEditor => editors.BooleanEditorComponent(x, data_handler)
        case x: F.FloatEditor => editors.FloatEditorComponent(x, data_handler)
        case x: F.IntegerEditor => editors.IntegerEditorComponent(x, data_handler)
        case x: F.SelectEditor => editors.SelectEditorComponent(x, data_handler)
      }
    }

    def size_hint(elem: F.FormElement): L.SizeHint = elem match {
      case _: F.FormGroup =>  L.FullWidth
      case _: F.FieldEditor => L.Medium
      case _: F.GroupArray => L.FullWidth
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
        .map(elem => render_child(elem, p, child_options))
      val layout = p.options.get(_.form_group_layout)
      val title = FormRenderingRules.show_title.apply_or_default(p.group) match {
        case false => None
        case true => Some(p.group.descr)
      }
      <.div(layout(title, children, p.options))
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("FormGroup")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(group: F.FormGroup, data_handler: FormDataHandler)(options: FormRenderingOptions): Unmounted = {
    component.apply(Props(group, data_handler, options))
  }
}
