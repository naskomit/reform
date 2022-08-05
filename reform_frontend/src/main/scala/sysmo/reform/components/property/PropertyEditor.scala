package sysmo.reform.components.property

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.shared.property.PropertySource

import scala.concurrent.Future

object PropertyEditor extends ReactComponent {
  case class Props(source: PropertySource)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      ColumnsPropertyLayout(p.source, FormRenderingOptions.default)
    }
    object Effects {
    }
  }


  val component =
    ScalaComponent.builder[Props]("PropertyEditor")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(source: PropertySource): Unmounted = {
    component(Props(source))
  }
}
