package sysmo.coviddata.services

import autowire.Client
import upickle.{default => upd}
import org.scalajs.dom
import ujson.{Obj, Value}

import scala.scalajs.js
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue

object DataApiClient extends Client[Value.Value, upd.Reader, upd.Writer] {

  def write[Result: upd.Writer](r: Result) = upd.writeJs(r)
  def read[Result: upd.Reader](p: Value.Value) = upd.read[Result](p)

  override def doCall(req: Request): Future[Value.Value] = {
    var req_opts = (new js.Object).asInstanceOf[dom.RequestInit]
    req_opts.method = dom.HttpMethod.POST
    val body = Obj(
      "path" -> req.path,
      "args" -> req.args
    )
    req_opts.body = upd.write(body)
    dom.console.log(req_opts)
    dom.fetch("/data/api", req_opts).toFuture.flatMap(x => {
      dom.console.log(x)
      x.text().toFuture
    }).map(x => ujson.read(x))
  }
}
