package sysmo.reform.services

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._
import io.circe.parser._
import scala.concurrent.Future
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import scala.scalajs.js
import org.scalajs.dom

class ApiClient(url: String) extends autowire.Client[Json, Decoder, Encoder] {
  def write[Result](r: Result)(implicit enc : Encoder[Result]) = enc.apply(r)
  def read[Result](p: Json)(implicit dec : Decoder[Result]) = p.as[Result] match {
    case Left(e) => throw e
    case Right(v) => v
  }

  override def doCall(req: Request): Future[Json] = {
    var req_opts = (new js.Object).asInstanceOf[dom.RequestInit]
    req_opts.method = dom.HttpMethod.POST
    val body = Json.obj(
      "path" -> req.path.asJson,
      "args" -> req.args.asJson
    )
    req_opts.body = write(body).toString
    dom.fetch(url, req_opts).toFuture.flatMap(x => {
      x.text().toFuture
    }).map(x => parse(x).getOrElse(throw new RuntimeException("Cannot parse the response!")))
  }

}

object ApiClient {
  def apply(url: String): ApiClient = new ApiClient(url)
}