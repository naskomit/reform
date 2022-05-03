package sysmo.reform.services

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
      .flatMap(parsed => {
        val cursor = parsed.hcursor
        val path_eth = cursor.downField("path").as[Seq[String]]
        val args_eth = cursor.downField("args").as[Map[String, Json]]
        (path_eth, args_eth) match {
          case (Right(path), Right(args)) => Right((path, args))
          case (_, _) => Left(new IllegalArgumentException("Failed parsing arguments"))
        }
      }).flatMap {case (path, args) => {
        route_map.get(path)
          .toRight(new RuntimeException(f"Cannot find method for path $path"))
          .map((_, args))
      }} match {
        case Right((method, args)) => method(args).map(_.spaces2)
        case Left(err) => Future.failed(err)
      }
  }
}
