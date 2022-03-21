package sysmo.reform.components.chart

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.plots.Plotly
import sysmo.reform.shared.{chart => Ch}
import sysmo.reform.services.ChartService

//import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue

//import scala.scalajs.js
//import scala.util.{Failure, Success, Try}


object ChartContainer extends ReactComponent {
  import japgolly.scalajs.react._

  case class Props(chart_request: Ch.ChartRequest, chart_service: ChartService)
  case class State(chart_result: Option[Ch.ChartResult])

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      Chart(s.chart_result)
    }

    def perform_request(p: Props): Callback =
      AsyncCallback.fromFuture(p.chart_service.chart(p.chart_request))
        .flatMap(res => {
          $.modState(s => s.copy(chart_result = Some(res))).asAsyncCallback
        }).handleError(err => {
        logger.error(err.getMessage)
        $.modState(s => s.copy(chart_result = None)).asAsyncCallback
      }).toCallback

  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("Form1")
    .initialState(State(None))
    .renderBackend[Backend]
    .componentDidMount(f => f.backend.perform_request(f.props))
    .build

  //
  def apply(chart_request: Ch.ChartRequest, chart_service: ChartService): Unmounted = {
    component(Props(chart_request, chart_service))
  }
}
