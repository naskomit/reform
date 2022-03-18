package sysmo.reform.components

import japgolly.scalajs.react.component.Scala
import sysmo.reform.components.plots.Plotly.{Backend, Props, State}


trait ReactComponent {
  type Props <: Product
  type State <: Product
  type Backend
  type Unmounted = Scala.Unmounted[Props, State, Backend]
}
