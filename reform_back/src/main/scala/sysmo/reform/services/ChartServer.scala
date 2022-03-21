package sysmo.reform.services

import java.lang.RuntimeException

import io.circe.Json
import io.circe.syntax._
import io.circe.parser
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.shared.{chart => Ch}
import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.shared.{query => Q}
import sysmo.reform.charts.Generator

import scala.concurrent.{ExecutionContext, Future}
import sysmo.reform.shared.chart.Transport._

class ChartServer(base_path: Seq[String], app_storage: GraphAppStorage)
                 (implicit val ec: ExecutionContext) extends APIServer {
  val route_map = Map(
    (base_path :+ "chart") ->
      (args => args("chart_req").as[Ch.ChartRequest].toTry.toFuture
        .flatMap(chart).map(x => x.asJson))
  )

  def chart(chart_req: Ch.ChartRequest): Future[Ch.ChartResult] = {

    try {
      val result = sdt.with_table_manager()(tm => {
        val data = chart_req.data.map {case (id, source) => source match {
          case Ch.QuerySource(q) => {
            val result = app_storage.query_table(q, tm)
            (id, result)
          }
        }}.toMap[String, sdt.Table]


        val result_builder = Ch.ChartResult.builder
        chart_req.charts.map(chart_def => {
          result_builder ++ Generator(data, chart_def)
        })

        result_builder.build
      })
      Future.successful(result)
    } catch {
      case err: RuntimeException => Future.failed(err)
    }

//    val x = Seq(1, 1, 4, 2, 1, 4, 2, 1, 3)
//    println(chart_req)
//
//    val data = Seq(
//      Histogram(x)
//    )
//    val layout = Layout() //.withTitle("Bars")
//    val config = Config()
//    val plot = Plotly.jsonSnippet(data, layout, config)
//    val plot_json = parser.parse(plot).getOrElse(Json.obj())
//
//    Future.successful(
//      Ch.ChartResult.builder
//        .add("one" -> Ch.Plotly(plot_json)).label("First Plot")
//        .build
//    )
  }


//    val result = Future(
//      """
//        |{"data":[{"type":"histogram","x":[1.0,1.0,4.0,2.0,1.0,4.0,2.0,1.0,3.0]}],"layout":{"title":"Bars"},"config":{}}
//      """.stripMargin)
//    result.flatMap(res => io.circe.parser.parse(res) match {
//      case Right(x) => Future.successful(x)
//      case Left(err) => Future.failed(err)
//    }).map(json =>
//      Ch.ChartResult.builder.add("First Plot" -> Ch.Plotly(json)).build
//    )
//  }

}
