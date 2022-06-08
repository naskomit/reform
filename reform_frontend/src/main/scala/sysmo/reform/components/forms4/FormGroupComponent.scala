package sysmo.reform.components.forms4

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.components.forms4.{layouts => L}
import sysmo.reform.shared.{form => F}

object FormGroupComponent extends ReactComponent {
  import japgolly.scalajs.react._

  case class Props(group: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val child_options = p.options.update(_.depth := p.options.get(_.depth) + 1)
      val children = p.group.elements
        .filter(elem => elem.show(p.data_handler.context(p.group)) match {
          case Right(x) => x
          case Left(err) => {
            logger.error(err)
            true
          }
        })
        .map(elem => L.GroupChildElement(elem, p.data_handler, child_options))
      val layout = p.options.get(_.form_group_layout)
      <.div(layout(p.group.descr, children, p.options))
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("FormGroup")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(group: F.FormGroup, data_handler: FormDataHandler, options: FormRenderingOptions): Unmounted = {
    component(Props(group, data_handler, options))
  }
}
