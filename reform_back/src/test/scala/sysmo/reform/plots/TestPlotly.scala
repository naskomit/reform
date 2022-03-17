package sysmo.reform.plots

//import plotly._, element._, layout._, Plotly._

import plotly.layout.Layout
import plotly.{Histogram, Scatter, Plotly, Config}



object TestPlotly {
  import Plotly._

  def test_scatter(): Unit = {
    val x = (0 to 100).map(_ * 0.1)
    val y1 = x.map(d => 2.0 * d + util.Random.nextGaussian())
    val y2 = x.map(math.exp)

    val plot = Seq(
      Scatter(x, y1).withName("Approx twice"),
      Scatter(x, y2).withName("Exp")
    )

    val lay = Layout().withTitle("Curves")
    plot.plot("plot.html", lay)

  }

  def test_bar(): Unit = {
    val x = Seq(1,1,4,2,1,4,2,1,3)

    val plot = Seq(
      Histogram(x)
    )

    val layout = Layout().withTitle("Bars")
    val config = Config()
//    plot.plot("plot.html", lay)
    plot.plot("plot.html", layout)
    println(Plotly.jsonSnippet(plot, layout, config))
  }
}
