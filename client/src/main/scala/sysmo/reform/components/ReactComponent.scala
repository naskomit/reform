package sysmo.reform.components

import japgolly.scalajs.react.component.Scala
import sysmo.reform.util.log.Logging

trait ReactComponent extends Logging {
  type Props <: Product
  type State <: Product
  type Backend
  type Unmounted = Scala.Unmounted[Props, State, Backend]
}
