package sysmo.reform.react

import japgolly.scalajs.react
import japgolly.scalajs.react.{BackendScope, CtorType}
import japgolly.scalajs.react.callback.{Exports => cb_exports}
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.Scala.Component
import sysmo.reform.shared.logging.Logging

trait ReactAction

trait ReactComponent extends Logging with cb_exports {
  type Props
  type State
  type Backend
  type BScope = BackendScope[Props, State]
  type Unmounted = Scala.Unmounted[Props, State, Backend]
  val ScalaComponent = react.ScalaComponent
  val component: Component[Props, State, Backend, CtorType.Props]
}