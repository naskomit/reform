package sysmo.coviddata.controllers

import javax.inject._
import sysmo.coviddata.shared.SharedMessages
import sysmo.coviddata.controllers.DataApiServer
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import ujson.{Value, read, write}

import scala.collection.mutable

@Singleton
class Application @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index(SharedMessages.itWorks))
  }

  def data_api = Action.async {req => {
    import autowire._
    val body = read(req.body.asText.get)
//    println("=========================")
//    println(req.body.asText.get)
//    println(body)
//    println(body("path").arr.map(x => x.str).toSeq)
//    println("=========================")
    val path = body("path").arr.map(_.str).toSeq
    val args = body("args").obj.toMap
    new DataApiServer().routes(Core.Request(path, args)).map(
      txt => Ok(ujson.write(txt))
    )}
  }

}
