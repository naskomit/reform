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
      <.div(
        <.h3(s"#${p.index} ",
          ButtonToolbar.builder
            .dropdown(<.i(^.className:="fa fa-edit fa-x3"))
            .button("Insert before", Effects.insert_before(p))
            .button("Insert after", Effects.insert_after(p))
            .button("Remove", Effects.remove(p))
            .build.component,
        ),

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
      val menu = ButtonToolbar.builder
        .dropdown(<.i(^.className:="fa fa-edit fa-x3"))
        .button("Append", Effects.append(p))
        .build
      layout(children.toSeq, Some(menu), p.options)
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
