package sysmo.reform.react.property

import sysmo.reform.react.ReactComponent
import sysmo.reform.shared.sources.property.{LocalPropertySource, PropertySource, PropertyTypes}
import japgolly.scalajs.react.vdom.html_<^._

case class PropertySourceView()
case class PropertyView()

class PropertyEditorF[PT <: PropertyTypes, F[+_]] extends ReactComponent {
  case class Props(ps: PropertySource[PT, F])
  case class State(ps_local: Option[LocalPropertySource[PT, F]])
  class Backend($: BScope) {
    def render(): VdomElement = {
      <.div("Props")
    }
  }

  val component = ScalaComponent.builder[Props]("ColumnsLayoutComponent")
      .initialState(State(None))
      .renderBackend[Backend]
      .build

  def apply(ps: PropertySource[PT, F]): Unmounted =
    component(Props(ps))
}
