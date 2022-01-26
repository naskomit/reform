package sysmo.coviddata.router

import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.extra.router.SetRouteVia.HistoryReplace
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.coviddata.panels.HomePanel
import sysmo.coviddata.IntuitiveLayout



object Form1Panel {
  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div("Form1")
    }
  }

  val component = ScalaComponent.builder[Props]("Form1")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}



object RouterConfiguration {
  val config = RouterConfigDsl[Pages].buildConfig { dsl =>
    import dsl._

    (emptyRule
      | staticRoute("#Home", HomePage) ~> render(HomePanel())
      | staticRoute("#Form1", Form1Page) ~> render(Form1Panel())
    )
    .notFound(_ => redirectToPage(HomePage)(HistoryReplace))
    .setTitle(p => s"${p.name} | SysMo").renderWith(layout)
  }

  def layout(router: RouterCtl[Pages], resolution: Resolution[Pages]) = {
    IntuitiveLayout(IntuitiveLayout.Props(router, resolution))
  }

}
