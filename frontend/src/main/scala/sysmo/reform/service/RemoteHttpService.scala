package sysmo.reform.service

import cats.MonadThrow
import cats.syntax.all._
import io.circe.parser.decode
import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import org.scalajs.dom
import org.scalajs.dom.HttpMethod
import sysmo.reform.shared.containers.FRemote
import sysmo.reform.shared.containers.implicits._
import sysmo.reform.shared.logging.Logging
import sysmo.reform.shared.util.Injector
import sysmo.reform.shared.service.{Err, Ok, RemoteResult, RemoteService, ServiceError}
import RemoteResult.Transport._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

class RemoteHttpService(val base_path: Option[String])
  extends RemoteService[FRemote] with Logging {
  override type MethodId = String
  implicit val ec: ExecutionContext = Injector.inject_option[ExecutionContext] match {
    case Left(err) => throw (err)
    case Right(value) => value
  }

  val mt: MonadThrow[FRemote] = MonadThrow[FRemote]

  protected def make_path(rel_path: String): String =
    base_path match {
      case Some(base) => base + "/" + rel_path
      case None => rel_path
    }

  def do_call[I: Encoder, O: Decoder](method_id: MethodId, req: I): FRemote[O] = {
    val request_options = (new js.Object).asInstanceOf[dom.RequestInit]
    request_options.body = req.asJson.toString()
    request_options.method = HttpMethod.POST
    FRemote.from_future(
      dom.fetch(make_path(method_id), request_options).toFuture
      .flatMap { x =>
        x.text().toFuture
      }
    ).flatMap { x =>
      decode[RemoteResult[O]](x) match {
        case Left(error) => mt.raiseError(error)
        case Right(value) => mt.pure(value)
      }
    }.flatMap {
      case Err(msg, stacktrace) => mt.raiseError(ServiceError(msg, stacktrace))
      case Ok(value) => mt.pure(value)
    }.handleErrorWith { error => error match {
      case ServiceError(msg, stacktrace) => {
        logger.error("Remote service error")
        logger.error(msg)
        if (stacktrace.length > 0) {
          logger.error("Stacktrace:")
          logger.error(stacktrace.asJson)
        }
      }
      case err => logger.error(err)
    }
      mt.raiseError(error)
    }
  }
}
