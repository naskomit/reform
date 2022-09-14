package sysmo.reform.app

import japgolly.scalajs.react.extra.router.{Resolution, RouterConfigDsl, RouterCtl}
import japgolly.scalajs.react.extra.router.SetRouteVia.HistoryReplace
import sysmo.reform.shared.logging.Logging

class RouterConfiguration(pages: PageCollection, app_config: Configuration, layout: Layout )
  extends Logging {
  val config = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    pages.collect.foldLeft(emptyRule)((acc, page) =>
      acc | staticRoute(page.url, page) ~> render(page.panel.apply(app_config))
    )
      .notFound(x => {
        logger.error(s"$x not found")
        redirectToPage(pages.home)(HistoryReplace)
      })
      .setTitle(p => s"${p.name} | SysMo").renderWith(create_layout)
  }

  private def create_layout(router: RouterCtl[Page], resolution: Resolution[Page]) = {
    layout(router, resolution, pages)
  }

}

object RouterConfiguration {
  def apply(pages: PageCollection, app_config: Configuration, layout: Layout): RouterConfiguration =
    new RouterConfiguration(pages, app_config, layout)
}