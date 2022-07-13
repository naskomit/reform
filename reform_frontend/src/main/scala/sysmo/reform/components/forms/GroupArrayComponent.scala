package sysmo.reform.components.forms

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms.editors.AbstractFormComponent
import sysmo.reform.components.forms.layouts.ArrayChildElement
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.menu.ButtonToolbar
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}

object ArrayItemComponent extends ReactComponent {
  case class Props(array: FR.Array, element: FR.Group, index: Int, options: FormRenderingOptions)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val menu_builder = ButtonToolbar.builder
        .dropdown(<.i(^.className:="fa fa-edit fa-x3"))

      p.array.prototype.prototype match {
        case group: FB.FieldGroup => {
          menu_builder.button("Insert before", Effects.insert_before(p))
          menu_builder.button("Insert after", Effects.insert_after(p))
        }
        case union: FB.GroupUnion => {
          union.subtypes.foreach{(group: FB.FieldGroup) =>
            menu_builder.button(s"Insert ${group.symbol} before", Effects.insert_before(p, Some(group.symbol)))
            menu_builder.button(s"Insert ${group.symbol} after", Effects.insert_before(p, Some(group.symbol)))
          }
        }
      }

      val menu_component = menu_builder.button("Remove", Effects.remove(p))
        .build.component

      <.div(
        <.h3(s"#${p.index} ", menu_component),
        FormGroupComponent(p.element, p.options)
      )
    }

    object Effects {
      def insert_before(p: Props, concrete_type: Option[String] = None): ButtonToolbar.CB = Callback {
        p.array.runtime.dispatch(FR.InsertElementBefore(p.array.id, p.element.id, concrete_type))
      }.asAsyncCallback

      def insert_after(p: Props, concrete_type: Option[String] = None): ButtonToolbar.CB = Callback {
        p.array.runtime.dispatch(FR.InsertElementAfter(p.array.id, p.element.id, concrete_type))
      }.asAsyncCallback

      def remove(p: Props, concrete_type: Option[String] = None): ButtonToolbar.CB = Callback {
        p.array.runtime.dispatch(FR.RemoveElement(p.array.id, p.element.id))
      }.asAsyncCallback

    }

  }


  val component =
    ScalaComponent.builder[Props]("ArrayItem")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(array: FR.Array, element: FR.Group, index: Int, options: FormRenderingOptions): Unmounted = {
    component(Props(array, element, index, options))
  }
}

object GroupArrayComponent extends AbstractFormComponent[FR.Array, FB.GroupArray] {
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val layout = p.options.get(_.group_array_layout)
      val children = p.obj.element_iterator
        .zipWithIndex
        .map(item => ArrayItemComponent.component.withKey(item._1.id.toString)(
          ArrayItemComponent.Props(p.obj, item._1, item._2, p.options)
        )).map(item => ArrayChildElement(item))

      val menu_builder = ButtonToolbar.builder
        .dropdown(<.i(^.className:="fa fa-edit fa-x3"))

      p.obj.prototype.prototype match {
        case group: FB.FieldGroup => menu_builder.button("Append", Effects.append(p))
        case union: FB.GroupUnion => {
          union.subtypes.foreach{(group: FB.FieldGroup) =>
            menu_builder.button(s"Append ${group.symbol}", Effects.append(p, Some(group.symbol)))
          }
        }
      }


      layout(children.toSeq, Some(menu_builder.build), p.options)
    }

    object Effects {
      def append(p: Props, concrete_type: Option[String] = None): ButtonToolbar.CB = Callback {
        p.obj.runtime.dispatch(FR.AppendElement(p.obj.id, concrete_type))
      }.asAsyncCallback
    }
  }



  val component =
    ScalaComponent.builder[Props]("GroupArray")
    .initialState(State())
    .renderBackend[Backend]
    .build

}
