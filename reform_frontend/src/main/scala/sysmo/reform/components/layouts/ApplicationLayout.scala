package sysmo.reform.components.layouts

import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import sysmo.reform.router.{Page, PageCollection}
import japgolly.scalajs.react.component.Scala
import sysmo.reform.components.ReactComponent

trait ApplicationLayout extends ReactComponent {
  def apply(router: RouterCtl[Page], resolution: Resolution[Page], pages: PageCollection): Scala.Unmounted[Props, State, Backend]
}
