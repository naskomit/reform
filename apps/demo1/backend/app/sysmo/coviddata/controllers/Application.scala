package sysmo.coviddata.controllers

import javax.inject._
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Application @Inject()(cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {
//  val app_storage = GraphAppStorage(config.underlying.getConfig("storage.orientdb"), CovidDatabaseSchema)
//  val base_path: Seq[String] = Seq("sysmo", "reform", "services")
//  val table_api_server = new TableApiServer(base_path :+ "TableDataService", app_storage)
//  val chart_api_server = new ChartServer(base_path :+ "ChartService", app_storage)

  def index = Action {
    Ok(views.html.index("It works"))
  }



//  def table_api = Action.async(req =>
//    table_api_server
//      .handle_async(req.body.asText)
//      .map(x => Ok(x))
//  )
//
//  def chart_api = Action.async(req =>
//    chart_api_server
//      .handle_async(req.body.asText)
//      .map(x => Ok(x))
//  )
}