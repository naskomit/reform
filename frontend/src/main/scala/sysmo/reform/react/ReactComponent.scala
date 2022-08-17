package sysmo.reform.react

import japgolly.scalajs.react.callback.{Exports => cb_exports}
import japgolly.scalajs.react.component.Scala
import sysmo.reform.util.log.Logging

trait ReactAction

trait ReactComponent extends Logging with cb_exports {
  type Props
  type State
  type Backend
  type Unmounted = Scala.Unmounted[Props, State, Backend]

}