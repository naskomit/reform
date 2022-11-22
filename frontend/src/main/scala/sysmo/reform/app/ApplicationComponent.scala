package sysmo.reform.app

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.widgets.ReactComponent

object ApplicationComponent extends ReactComponent {

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div("This is a template component")
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("ApplicationComponent")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(router: RouterCtl[Page], resolution: Resolution[Page], pages: PageCollection): Unmounted = {
    component(Props())
  }
}
