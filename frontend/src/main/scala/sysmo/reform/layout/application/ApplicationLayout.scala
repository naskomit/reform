package sysmo.reform.layout.application

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import sysmo.reform.app.{Page, PageCollection}
import sysmo.reform.widgets.ReactComponent

trait ApplicationLayout extends ReactComponent {
  def apply(router: RouterCtl[Page], resolution: Resolution[Page], pages: PageCollection): Scala.Unmounted[Props, State, Backend]
}
