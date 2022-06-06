package sysmo.reform.components.forms4

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.shared.data.{form4 => F}

object ArrayItemComponent extends ReactComponent {
  case class Props(group: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      FormGroupComponent(p.group, p.data_handler, p.options)
    }
  }

  val component =
    ScalaComponent.builder[Props]("ArrayItem")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(group: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions): Unmounted = {
    component(Props(group, data_handler, options))
  }
}

object GroupArrayComponent extends ReactComponent {

  case class Props(array: F.GroupArray, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(
        p.array.elements(p.data_handler.current_data).toVdomArray(
          item => ArrayItemComponent.component.withKey(item.fid.toString)(
            ArrayItemComponent.Props(item, p.data_handler, p.options)
          )
        )
      )
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
