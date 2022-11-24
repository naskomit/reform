package sysmo.coviddata.controllers

import cats.MonadThrow
import cats.syntax.all._

import javax.inject._
import play.api.Configuration
import play.api.mvc._
import sysmo.reform.server.OrientDBReformServer
import sysmo.reform.shared.containers.{FLocal, RFContainer}
import sysmo.reform.shared.containers.implicits._
import sysmo.reform.shared.examples.SkullInventoryBuilder
import sysmo.reform.shared.types.TypeSystem

import scala.concurrent.{ExecutionContext, Future}
object SkullInventoryReformServer extends OrientDBReformServer[FLocal] {
  override val mt = MonadThrow[FLocal]
  override val type_system: TypeSystem = SkullInventoryBuilder.type_builder.build
}

@Singleton
class Application @Inject()(cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  private val rf_server = SkullInventoryReformServer

  def respond[T, F[+T] <: RFContainer[T]](result: F[T])(implicit mt: MonadThrow[F]): Future[Result] = {
    result
      .map(x => Ok(x.toString))
      .handleError { err =>
        val err_msg =
          if (err.getMessage == null) {
            err.getClass.getName
          } else {
            err.getMessage
          }
        System.err.println(err_msg)
        Ok(err_msg)
      }
      .to_future
  }

  def async_handler[F[+T] <: RFContainer[T]](handler: String => F[String])(implicit mt: MonadThrow[F]): Action[AnyContent] = {
    Action.async {req =>
      respond(for {
        body <- req.body.asText match {
          case Some(x) => mt.pure(x)
          case None => mt.raiseError(new IllegalArgumentException("Request must have a body"))
        }
        result <- handler(body)
      } yield result)
    }
  }

  def home: Action[AnyContent] = Action {
    Ok(views.html.index(s"Starting up the home application"))
  }

  def index(app: String): Action[AnyContent] = Action {
    Ok(views.html.index(s"Starting up the application: ${app}"))
  }



  def query(app: String): Action[AnyContent] = async_handler(rf_server.handle_query)

}