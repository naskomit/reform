package sysmo.coviddata.panels

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.chart.ChartContainer
import sysmo.reform.shared.{chart => Ch}
import sysmo.reform.shared.{query => Q}
import sysmo.reform.components.{ApplicationPanel, ReactComponent}
import sysmo.reform.services.ServerChartService
import sysmo.reform.shared.chart.ChartRequest

object ChartPanel extends ApplicationPanel {
  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(
        ChartContainer(ChartRequest(
          Map(
            "Clinical" -> Ch.QuerySource(Q.BasicQuery(
              Q.SingleTable("Clinical"), Some(Seq(Q.ColumnRef("18")))
            ))
          ),
          Seq(Ch.Histogram("Clinical", "18"))
        ), ServerChartService))
    }

    def init(p: Props): Callback = Callback {

    }
  }


  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("Form1")
      .initialState(State())
      .renderBackend[Backend]
      .componentDidMount(f => f.backend.init(f.props))
      .build

  def apply(): Scala.Unmounted[Props, State, Backend] = {
    component(Props())
  }
}



