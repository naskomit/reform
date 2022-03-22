package sysmo.reform.components.chart

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.plots.Plotly
import sysmo.reform.shared.util.NamedValue
import sysmo.reform.shared.{chart => Ch}

object Chart extends ReactComponent {
  import japgolly.scalajs.react._

  case class Props(chart_result: Option[Ch.ChartResult])
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val children: Seq[TagMod] = p.chart_result match {
        case None => Seq(<.div("Nothing to show"))
        case Some(Ch.ChartResult(items)) => items.map {
          case child@ NamedValue(name, label, content: Ch.Plotly) => {
            val label = child.make_label
            <.div(<.h2(label), Plotly("800", "600", content))
          }

        }
      }
      <.div(children.toTagMod)
    }

  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("Form1")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(chart_result: Option[Ch.ChartResult]): Unmounted = {
    component(Props(chart_result))
  }
}
