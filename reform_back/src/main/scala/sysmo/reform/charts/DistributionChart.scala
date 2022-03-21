package sysmo.reform.charts

import sysmo.reform.shared.{chart => Ch}
import sysmo.reform.shared.data.{table => sdt}
import io.circe.{parser, Json}
import plotly.layout.Layout
import plotly.{Config, Histogram, Plotly}


object DistributionChart {
  def generate(data: Map[String, sdt.Table], chart_def: Ch.Histogram): Ch.ChartResult = {

    val x = data.get(chart_def.data_id)
      .map(table => table.column(chart_def.column_id))
      .map(column => Util.plotly_series(column))
      .get
//      .map(column => column.iterator
//        .filter((element: sdt. Value) => element.is_set)
//        .map((element : sdt.Value) => element.v.get))
//      .get.toSeq

    val traces = Seq(
      Histogram(x)
    )
    val layout = Layout() //.withTitle("Bars")
    val config = Config()
    val plot = Plotly.jsonSnippet(traces, layout, config)
    val plot_json = parser.parse(plot).getOrElse(Json.obj())

    Ch.ChartResult.builder
        .add("one" -> Ch.Plotly(plot_json)).label("First Plot")
        .build
  }
}
