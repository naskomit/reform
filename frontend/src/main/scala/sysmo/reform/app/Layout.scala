package sysmo.reform.app

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import sysmo.reform.react.ReactComponent

trait Layout extends ReactComponent {
  def apply(router: RouterCtl[Page], resolution: Resolution[Page], pages: PageCollection): Scala.Unmounted[Props, State, Backend]
}
