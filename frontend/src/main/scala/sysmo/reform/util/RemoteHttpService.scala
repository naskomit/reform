package sysmo.reform.util

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._
import io.circe.parser.decode
import org.scalajs.dom

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

class RemoteHttpService(implicit ec: ExecutionContext) {
  type F[+X] = Future[X]
  protected def do_call[I: Encoder, O: Decoder](url: String, req: I): Future[O] = {
    val request_options = (new js.Object).asInstanceOf[dom.RequestInit]
    request_options.body = req.asJson.toString()
    dom.fetch(url, request_options).toFuture
      .flatMap(x => x.text().toFuture)
      .flatMap(x => decode(x) match {
        case Left(error) => Future.failed(error)
        case Right(value) => Future.successful(value)
      })
  }

}
