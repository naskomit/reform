package sysmo.coviddata.controllers

import javax.inject._
import play.api.Configuration
import sysmo.coviddata.shared.SharedMessages
import play.api.mvc._
import sysmo.reform.db.GraphAppStorage

import scala.concurrent.{ExecutionContext, Future}
import upickle.default._
import ujson.Value


@Singleton
class Application @Inject()(cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  val app_storage = GraphAppStorage(config.underlying.getConfig("storage.orientdb"))
  val base_path: Seq[String] = Seq("sysmo", "coviddata", "shared", "data", "PatientData")
  val data_api_server = new DataApiServer(base_path, app_storage)(ec)

  def index = Action {
    Ok(views.html.index(SharedMessages.itWorks))
  }

  def data_api = Action.async {req => {
    import autowire._
    val body = read[Value](req.body.asText.get)
    val path = body("path").arr.map(_.str).toSeq
    val args = body("args").obj.toMap
    data_api_server.routes(Core.Request(path, args)).map(
      value => Ok(ujson.write(value))
    )}
  }

}
