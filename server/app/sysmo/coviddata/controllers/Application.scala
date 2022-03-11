package sysmo.coviddata.controllers

import javax.inject._
import play.api.Configuration
import sysmo.coviddata.shared.SharedMessages
import play.api.mvc._
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.services.TableApiServer
import io.circe.parser
import io.circe.syntax._
import io.circe.Json
import autowire.Core
import monix.eval.Task

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


@Singleton
class Application @Inject()(cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  val app_storage = GraphAppStorage(config.underlying.getConfig("storage.orientdb"))
  val base_path: Seq[String] = Seq("sysmo", "reform", "shared", "data", "TableData")
  val table_api_server = new TableApiServer(base_path, app_storage)(ec)

  def index = Action {
    Ok(views.html.index(SharedMessages.itWorks))
  }



  def data_api = Action.async(req => {
    val result = req.body.asText.
      toRight(new RuntimeException("No request body!"))
        .flatMap(parser.parse).toTry.flatMap(body => {
      val cursor = body.hcursor
      val path_eth = cursor.downField("path").as[Seq[String]]
      val args_eth = cursor.downField("args").as[Map[String, Json]]
      (path_eth, args_eth ) match {
        case (Right(path), Right(args)) => Success((path, args))
        case (_, _) => Failure(new IllegalArgumentException("Failed parsing arguments"))
      }
    }) match {
      case Success((path, args)) =>
        table_api_server.routes(Core.Request(path, args)).map(x => x.spaces2)
      case Failure(e) => Future.failed(e)
    }

    result.map(x => Ok(x))




//    match {
//      case Success((path, args) =>
//      table_api_server.routes(Core.Request(path, args))
//    } match {
//      case Right(x) => Future.successful(Ok("Hello")) //table_api_server.routes(Core.Request(path, args))
//      case Left(e) => Future.successful(InternalServerError("Failed"))
//    }
//    table_api_server.routes(Core.Request(path, args)).map(
//      value => Ok(value.asJson.spaces2)
//    )
//    Future(Ok("Hello"))
  })

//  def data_api = Action.async {req => {
////    import autowire._
////    req.body.asText.map(parser.parse).map(body => {
////      val path = body("path"). //arr.map(_.str).toSeq
////      val args = body("args").obj.toMap
////      data_api_server.routes(Core.Request(path, args))
////    }).map(
////      value => Ok(ujson.write(value))
////    )
//
//  }}

}
