package sysmo.reform.components

import japgolly.scalajs.react.component.Scala

trait ApplicationPanel extends ReactComponent {
  def apply(): Scala.Unmounted[Props, State, Backend]
}
