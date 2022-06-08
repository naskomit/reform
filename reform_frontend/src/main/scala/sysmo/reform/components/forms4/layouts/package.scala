package sysmo.reform.components.forms4

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.shared.{form => F}

package object layouts {
  sealed trait ElementWidth
  case object ExtraShort extends ElementWidth
  case object Short extends ElementWidth
  case object Medium extends ElementWidth
  case object Long extends ElementWidth
  case object ExtraLong extends ElementWidth
  case object FullWidth extends ElementWidth
  case class GroupChildElement(child: F.FormElement, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class ArrayChildElement(child: VdomNode)

  trait FormGroupLayout extends ReactComponent {
    def apply(title: String, children: Seq[GroupChildElement], options: FormRenderingOptions): Unmounted
    def render_child(child: GroupChildElement): VdomNode = {
      val data_handler = child.data_handler
      child.child match {
        case x: F.FieldEditor => render_field_editor(x, data_handler)
        case x: F.FormGroup => FormGroupComponent(x, data_handler, child.options)
        case x: F.GroupArray => GroupArrayComponent(x, data_handler, child.options)
      }
    }

    def render_field_editor(editor: F.FieldEditor, data_handler: FormDataHandler): VdomNode = {
      editor match {
        case x: F.StringEditor => editors.StringEditorComponent(x, data_handler)
        case x: F.BooleanEditor => editors.BooleanEditorComponent(x, data_handler)
        case x: F.FloatEditor => editors.FloatEditorComponent(x, data_handler)
        case x: F.IntegerEditor => editors.IntegerEditorComponent(x, data_handler)
        case x: F.SelectEditor => editors.SelectEditorComponent(x, data_handler)
      }
    }

    def size_hint(elem: F.FormElement): ElementWidth = elem match {
      case e: F.FormGroup =>  FullWidth
      case e: F.FieldEditor => Medium
      case e: F.GroupArray => FullWidth
    }
  }


  trait ArrayGroupLayout extends ReactComponent {
    def apply(title: String, children: Seq[ArrayChildElement], options: FormRenderingOptions): Unmounted
  }
}
