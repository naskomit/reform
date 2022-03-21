package sysmo.reform.services

import autowire.Core
import autowire.Core.Request
import io.circe.{Json, parser}

import scala.util.{Failure, Success, Try}
import scala.concurrent.{ExecutionContext, Future}

trait APIServer {
  type PickleType = Json
  protected val route_map: Map[Seq[String], Map[String, PickleType] => Future[PickleType]]
  implicit val ec: ExecutionContext

  implicit class Try2Future[A](x: Try[A]) {
    def toFuture: Future[A] = x match {
      case Success(res) => Future.successful(res)
      case Failure(err) => Future.failed(err)
    }
  }

  def handle_async(body: Option[String]): Future[String] = {
    body.toRight(new RuntimeException("No request body!"))
      .flatMap(parser.parse)
      .toTry.flatMap(parsed => {
        val cursor = parsed.hcursor
        val path_eth = cursor.downField("path").as[Seq[String]]
        val args_eth = cursor.downField("args").as[Map[String, Json]]
        (path_eth, args_eth) match {
          case (Right(path), Right(args)) => Success((path, args))
          case (_, _) => Failure(new IllegalArgumentException("Failed parsing arguments"))
        }
      }).flatMap {case (path, args) => {
        route_map.get(path)
          .toRight(new RuntimeException(f"Cannot find method for path $path"))
          .toTry.map((_, args))
      }} match {
        case Success((method, args)) => method(args).map(_.spaces2)
        case Failure(err) => Future.failed(err)
      }
  }
}
