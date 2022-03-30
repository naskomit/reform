package sysmo.reform.charts

import sysmo.reform.shared.{chart => Ch}
import sysmo.reform.shared.data.{table => sdt}
import io.circe.{Json, parser}
import plotly.layout.Layout
import plotly.{Bar, Box, Config, Histogram, Plotly, Trace}


object DistributionChart {
  def categorical(data: sdt.SeriesCategorical): Trace = {
    val cat_counts = data.count_categories
    val x = cat_counts.map(_._1)
    val y = cat_counts.map(_._2)
    Bar(x = Util.plotly_series(x), y = Util.plotly_series(y))
  }

  def continuous(data: sdt.Series): Trace = {
//    import breeze.stats.{mean, stddev,
    Box(Util.plotly_series(data))
  }

  def generate(data_source: Map[String, sdt.Table], chart_def: Ch.DistributionSettings): Ch.ChartResult = {

    val column = data_source.get(chart_def.data_id)
      .map(table => table.column(chart_def.column_id))
      .get
    val label = column.field.make_label

//    val data = Util.plotly_series(column)
//      .map(column => column.iterator
//        .filter((element: sdt. Value) => element.is_set)
//        .map((element : sdt.Value) => element.v.get))
//      .get.toSeq

    val traces =
      if (column.field.field_type.tpe == sdt.VectorType.Char)
        Seq(categorical(column.to_categorical()))
      else if (column.field.field_type.tpe == sdt.VectorType.Real)
        Seq(continuous(column))
      else if (column.field.field_type.tpe == sdt.VectorType.Int)
        Seq(continuous(column))
      else
        throw new IllegalStateException(s"Cannot create distribution chart for column ${chart_def.column_id}")

    val layout = Layout() //.withTitle("Bars")
    val config = Config()
    val plot = Plotly.jsonSnippet(traces, layout, config)
    val plot_json = parser.parse(plot).getOrElse(Json.obj())
    val uid = plot.hashCode.toString

    Ch.ChartResult.builder
        .add("distribution" -> Ch.Plotly(uid, plot_json)).label(s"Distribution of $label")
        .build
  }
}
