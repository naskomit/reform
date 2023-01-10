package sysmo.reform

import cats.MonadThrow
import play.api.Configuration
import play.api.mvc._
import cats.syntax.all._
import sysmo.reform.shared.containers.RFContainer
import sysmo.reform.shared.containers.implicits._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppController @Inject()(cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {
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
    Action.async { req =>
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
    Ok(views.html.index(s"Starting up the home application")("home"))
  }

  def redirect_index(app: String): Action[AnyContent] = Action {
    Redirect(routes.AppController.index(app))
  }


  def index(app: String): Action[AnyContent] = Action {
    Ok(views.html.index(s"Starting up the application: ${app}")(app))
  }


  def query(app: String): Action[AnyContent] = async_handler(rf_server.handle_query)

}
