package sysmo.coviddata.controllers

import javax.inject._
import play.api.Configuration
import play.api.mvc._
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.services.TableApiServer
import io.circe.parser
import io.circe.Json
import autowire.Core

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


@Singleton
class Application @Inject()(cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  val app_storage = GraphAppStorage(config.underlying.getConfig("storage.orientdb"))
  val base_path: Seq[String] = Seq("sysmo", "reform", "shared", "data", "TableData")
  val table_api_server = new TableApiServer(base_path, app_storage)(ec)

  def index = Action {
    Ok(views.html.index("It works"))
  }



  def data_api = Action.async(req => {
    val result = req.body.asText.
      toRight(new RuntimeException("No request body!"))
      .flatMap(parser.parse).toTry.flatMap(body => {
      val cursor = body.hcursor
      val path_eth = cursor.downField("path").as[Seq[String]]
      val args_eth = cursor.downField("args").as[Map[String, Json]]
      (path_eth, args_eth) match {
        case (Right(path), Right(args)) => Success((path, args))
        case (_, _) => Failure(new IllegalArgumentException("Failed parsing arguments"))
      }
    }) match {
      case Success((path, args)) =>
        table_api_server.routes(Core.Request(path, args)).map(x => x.spaces2)
      case Failure(e) => Future.failed(e)
    }

    result.map(x => Ok(x))
  })

  def plot_api = Action {
    import plotly.layout.Layout
    import plotly.{Config, Histogram, Plotly}

    val x = Seq(1,1,4,2,1,4,2,1,3)

    val plot = Seq(
      Histogram(x)
    )
    val layout = Layout().withTitle("Bars")
    val config = Config()
    Ok(Plotly.jsonSnippet(plot, layout, config))
  }
}