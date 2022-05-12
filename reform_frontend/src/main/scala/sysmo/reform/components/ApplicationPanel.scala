package sysmo.reform.components

import japgolly.scalajs.react.component.Scala
import sysmo.reform.ApplicationConfiguration

trait ApplicationPanel extends ReactComponent {
  def apply(app_config: ApplicationConfiguration): Unmounted
}
