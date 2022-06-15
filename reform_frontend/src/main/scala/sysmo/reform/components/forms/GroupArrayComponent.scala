package sysmo.reform.components.forms

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms.{editors => Edit}
import sysmo.reform.components.forms.layouts.ArrayChildElement
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.menu.ButtonToolbar
import sysmo.reform.shared.{form => F}

object ArrayItemComponent extends Edit.AbstractFormComponent {
  case class Props(array: F.GroupArray, element: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State(i: Int)
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(
        ButtonToolbar.builder
          .button("Insert before", Effects.insert_before(p))
          .button("Insert after", Effects.insert_after(p))
          .button("Remove", Effects.remove(p))
          .build,

        FormGroupComponent(p.element, p.data_handler)(p.options)
      )
    }

    object Effects {
      def array_id(p: Props): F.ArrayFieldId = {
        p.element.fid match {
          case id @ F.ArrayFieldId(_) => id
        }
      }

      def insert_before(p: Props): ButtonToolbar.CB = Callback {
        p.data_handler.dispatch(Edit.InsertElementBefore(p.array, array_id(p)))
      }.asAsyncCallback

      def insert_after(p: Props): ButtonToolbar.CB = Callback {
        p.data_handler.dispatch(Edit.InsertElementAfter(p.array, array_id(p)))
      }.asAsyncCallback

      def remove(p: Props): ButtonToolbar.CB = Callback {
        p.data_handler.dispatch(Edit.RemoveArrayElement(p.array, array_id(p)))
      }.asAsyncCallback

    }

  }


  val component =
    ScalaComponent.builder[Props]("ArrayItem")
      .initialState(State(0))
      .renderBackend[Backend]
      .build

//  def apply(array: F.GroupArray, element: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions): Unmounted = {
//    component(Props(array, element, data_handler, options))
//  }
}

object GroupArrayComponent extends ReactComponent {

  case class Props(array: F.GroupArray, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val layout = p.options.get(_.group_array_layout)
      val children: Seq[ArrayChildElement] = p.array.elements(p.data_handler.current_data)
        .map(item => ArrayItemComponent.component.withKey(item.fid.toString)(
          ArrayItemComponent.Props(p.array, item, p.data_handler, p.options)
        )).map(item => ArrayChildElement(item))
      val menu = ButtonToolbar.builder
        .button("Append", Effects.append(p))
        .build
      layout(Seq(ArrayChildElement(menu)) ++ children, p.options)
    }

    object Effects {
      def append(p: Props): ButtonToolbar.CB = Callback {
        p.data_handler.dispatch(Edit.AppendElement(p.array))
      }.asAsyncCallback
    }
  }



  val component =
    ScalaComponent.builder[Props]("GroupArray")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(array: F.GroupArray, data_handler: FormDataHandler)(options: FormRenderingOptions): Unmounted = {
    component(Props(array, data_handler, options))
  }
}
