package sysmo.coviddata.panels

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.chart.ChartContainer
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.managers.ChartManager
import sysmo.reform.shared.{chart => Ch}
import sysmo.reform.shared.data.{graph => G}
import sysmo.coviddata.shared.{data => CD}

import scala.scalajs.js

object ChartPanel extends ApplicationPanel {
  import japgolly.scalajs.react._

  case class Props()
  case class State()
  val clin_schema = G.Schema
    .table_schema_builder(CD.Clinical.schema)
    .build
  val socio_demographic_schema = G.Schema
    .table_schema_builder(CD.SocioDemographic.schema)
    .build

  val vmargin = 30
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {

      <.div(
        <.div(
          <.div(^.cls:= "page-title",
            <.h1("Charts")
          ),
          <.div(^.className:= "row", ^.style:= js.Dictionary("margin-top" -> vmargin, "margin-bottom" -> vmargin),
            <.div(^.className:= "col-md-1"),
            <.div(^.className:= "col-md-10",
              <.div(^.cls:= "wrapper wrapper-white",
                ChartContainer(
                  ChartManager.distribution(
                    Ch.DistributionSettings("Clinical", "18"),
                    Map("Clinical" -> clin_schema, "Socio-demographic" -> socio_demographic_schema)
                  ), height = 600
                )
              )

            )
          ),
          <.div(^.className:= "row", ^.style:= js.Dictionary("margin-top" -> vmargin, "margin-bottom" -> vmargin),
            <.div(^.className:= "col-md-1"),
            <.div(^.className:= "col-md-10",
              <.div(^.cls:= "wrapper wrapper-white",
                ChartContainer(
                  ChartManager.distribution(
                    Ch.DistributionSettings("SocioDemographic", "3"),
                    Map("Clinical" -> clin_schema, "Socio-demographic" -> socio_demographic_schema)
                  ), height = 600
                )
              )

            )
          )


        )
      )
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



