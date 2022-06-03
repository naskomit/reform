package sysmo.reform.components.forms4

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.shared.data.{form4 => F}

object ArrayItemComponent extends ReactComponent {
  case class Props(array: F.GroupArray, item_id: String, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      FormGroupComponent(p.array.group, p.data_handler, p.options)
    }
  }

  val component =
    ScalaComponent.builder[Props]("ArrayItem")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(array: F.GroupArray, item_id: String, data_handler: FormDataHandler, options: FormRenderingOptions): Unmounted = {
    component(Props(array, item_id, data_handler, options))
  }
}

object GroupArrayComponent extends ReactComponent {

  case class Props(array: F.GroupArray, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
//      val index = p.data_handler.get_value(p.p)
      FormGroupComponent(p.array.group, p.data_handler, p.options)
    }
  }

  val component =
    ScalaComponent.builder[Props]("GroupArray")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(array: F.GroupArray, data_handler: FormDataHandler, options: FormRenderingOptions): Unmounted = {
    component(Props(array, data_handler, options))
  }
}
