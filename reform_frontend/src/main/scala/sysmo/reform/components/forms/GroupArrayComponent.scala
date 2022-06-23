package sysmo.reform.components.forms

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms.editors.AbstractFormComponent
import sysmo.reform.components.forms.{editors => Edit}
import sysmo.reform.components.forms.layouts.ArrayChildElement
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.menu.ButtonToolbar
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}

object ArrayItemComponent extends ReactComponent {
  case class Props(array: FR.Array, element: FR.Group, options: FormRenderingOptions)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(
        ButtonToolbar.builder
          .button("Insert before", Effects.insert_before(p))
          .button("Insert after", Effects.insert_after(p))
          .button("Remove", Effects.remove(p))
          .build,

        FormGroupComponent(p.element, p.options)
      )
    }

    object Effects {
      def insert_before(p: Props): ButtonToolbar.CB = Callback {
        p.array.runtime.dispatch(FR.InsertElementBefore(p.array, p.element.id))
      }.asAsyncCallback

      def insert_after(p: Props): ButtonToolbar.CB = Callback {
        p.array.runtime.dispatch(FR.InsertElementAfter(p.array, p.element.id))
      }.asAsyncCallback

      def remove(p: Props): ButtonToolbar.CB = Callback {
        p.array.runtime.dispatch(FR.RemoveArrayElement(p.array, p.element.id))
      }.asAsyncCallback

    }

  }


  val component =
    ScalaComponent.builder[Props]("ArrayItem")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(array: FR.Array, element: FR.Group, options: FormRenderingOptions): Unmounted = {
    component(Props(array, element, options))
  }
}

object GroupArrayComponent extends AbstractFormComponent[FR.Array, FB.GroupArray] {
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val layout = p.options.get(_.group_array_layout)
      val children = p.obj.element_iterator
        .map(item => ArrayItemComponent.component.withKey(item.id.toString)(
          ArrayItemComponent.Props(p.obj, item, p.options)
        )).map(item => ArrayChildElement(item))
      val menu = ButtonToolbar.builder
        .button("Append", Effects.append(p))
        .build
      layout(Seq(ArrayChildElement(menu)) ++ children, p.options)
    }

    object Effects {
      def append(p: Props): ButtonToolbar.CB = Callback {
        p.obj.runtime.dispatch(FR.AppendElement(p.obj.id))
      }.asAsyncCallback
    }
  }



  val component =
    ScalaComponent.builder[Props]("GroupArray")
    .initialState(State())
    .renderBackend[Backend]
    .build

}
