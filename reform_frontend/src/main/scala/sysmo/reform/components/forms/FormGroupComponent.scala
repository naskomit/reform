package sysmo.reform.components.forms

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.forms.editors.AbstractFormComponent
import sysmo.reform.components.forms.layouts.{ColumnsLayoutComponent, GroupChildNode, TabbedLayoutComponent}
import sysmo.reform.components.forms.options.{FormRenderingOptions, FormRenderingRules}
import sysmo.reform.components.forms.{layouts => L}
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import japgolly.scalajs.react._

object FormGroupComponent extends AbstractFormComponent[FR.Group, FB.FieldGroup] {
  case class State()

  final class Backend($: BackendScope[Props, State]) {

    def render_child(element: FR.RuntimeObject, label: Option[String], child_options: FormRenderingOptions): GroupChildNode = {
//      val label = element.parent_rel match {
//        case Some(FR.ParentRelation(_, pr: FB.HasElement)) => Some(pr.descr)
//        case None => None
//      }
      val element_fn: Partial = element match {
        case x: FR.AtomicValue => render_field_editor(x)
        case x: FR.Group => FormGroupComponent.partial(x)
        case x: FR.Array => GroupArrayComponent.partial(x)
      }
      GroupChildNode(element_fn, label, size_hint(element), child_options)
    }


    def render_field_editor(field_obj: FR.AtomicValue): Partial = {
      field_obj.prototype match {
        case _: FB.StringField => editors.StringEditorComponent.partial(field_obj)
        case _: FB.BooleanField => editors.BooleanEditorComponent.partial(field_obj)
        case _: FB.FloatField => editors.FloatEditorComponent.partial(field_obj)
        case _: FB.IntegerField => editors.IntegerEditorComponent.partial(field_obj)
        case _: FB.SelectField => editors.SelectEditorComponent.partial(field_obj)
      }
    }

    def size_hint(elem: FR.RuntimeObject): L.SizeHint = elem match {
      case _: FR.Group =>  L.FullWidth
      case _: FR.AtomicValue => L.Medium
      case _: FR.Array => L.FullWidth
    }

    def render (p: Props, s: State): VdomElement = {
      val child_options = p.options.update(_.depth := p.options.get(_.depth) + 1)
      val children = p.obj.element_iterator
//TODO Visibility
        //        .filter(elem => elem.show(p.data_handler.context(p.group)) match {
//          case Right(x) => x
//          case Left(err) => {
//            logger.error(err)
//            true
//          }
//        })
        .map(elem => render_child(elem._2, Some(elem._1), child_options))
        .toSeq

      val layout = (p.obj.prototype.layout) match {
        case Some("linear") => ColumnsLayoutComponent
        case Some("tabbed") => TabbedLayoutComponent
        case None => p.options.get(_.form_group_layout)
      }

      <.div(layout(children, p.options))
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("FormGroup")
    .initialState(State())
    .renderBackend[Backend]
    .build

}
