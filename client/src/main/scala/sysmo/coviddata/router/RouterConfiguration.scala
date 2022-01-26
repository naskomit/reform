package sysmo.coviddata.router

import japgolly.scalajs.react.extra.router.SetRouteVia.HistoryReplace
import japgolly.scalajs.react.extra.router._
import sysmo.coviddata.layouts.IntuitiveLayout
import sysmo.coviddata.panels.{Form1Panel, HomePanel}

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
