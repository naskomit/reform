package sysmo.coviddata.controllers

import javax.inject._
import play.api.Configuration
import play.api.mvc._
import sysmo.reform.shared.query.Query
import sysmo.reform.shared.util.containers.FLocal
import sysmo.reform.shared.util.containers.implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Application @Inject()(cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  private val rf_server = SkullInventoryReformServer

  // final def async(block: => Future[Result]): Action[AnyContent]
  def async_handler[F[+_]](handler: String => F[String]): Action[AnyContent] = {
    Action.async {req =>
      FLocal.from_option(req.body.asText).map(x => Ok(x)).to_future
    }
  }

  def index: Action[AnyContent] = Action {
    Ok(views.html.index("It works"))
  }



  def query: Action[AnyContent] = async_handler(rf_server.handle_query)

}