package sysmo.reform.router

import japgolly.scalajs.react.extra.router.SetRouteVia.HistoryReplace
import japgolly.scalajs.react.extra.router._
import sysmo.coviddata.router.HomePage
import sysmo.reform.components.layouts.ApplicationLayout

class RouterConfiguration(pages: PageCollection, layout: ApplicationLayout ) {
  val config = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    pages.collect.foldLeft(emptyRule)((acc, page) =>
      acc | staticRoute(page.url, page) ~> render(page.panel.apply())
    )
    .notFound(_ => redirectToPage(HomePage)(HistoryReplace))
    .setTitle(p => s"${p.name} | SysMo").renderWith(create_layout)
  }

  private def create_layout(router: RouterCtl[Page], resolution: Resolution[Page]) = {
    layout(router, resolution, pages)
  }

}

object RouterConfiguration {
  def apply(pages: PageCollection, layout: ApplicationLayout): RouterConfiguration =
    new RouterConfiguration(pages, layout)
}