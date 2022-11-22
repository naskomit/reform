package sysmo.reform.app

import io.circe.Json
import japgolly.scalajs.react.extra.router.{BaseUrl, Router}
import org.scalajs.dom
import org.scalajs.dom.Event
import sysmo.reform.css.ReFormCSS
import sysmo.reform.shared.logging.{JsonPrinter, Logging, Printer}
import sysmo.reform.css.CssSettings._
import sysmo.reform.shared.util.Injector
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sysmo.reform.layout.application.ApplicationLayout

import scala.concurrent.ExecutionContext


trait UIApplication extends Logging {
  val react_node: String = "mainApp"
  def pages: PageCollection
  val layout: ApplicationLayout

  def main(args: Array[String]): Unit = {
    configure_printers()
    configure_execution_context()
    dom.window.addEventListener("load", init_react)
  }

  def configure_printers(): Unit = {
    import scalajs.js.JSON
    def to_js(x: Json): Dynamic = {
      val res = JSON.parse(x.toString())
//      println(res.getClass.getName)
//      println(res)
      res
    }

    val printer = new JsonPrinter {
      override def out(msg: Json): Unit =
        dom.console.log(to_js(msg))
      override def warn(msg: Json): Unit =
        dom.console.warn(to_js(msg))
      override def error(msg: Json): Unit =
        dom.console.error(to_js(msg))
      override def out(msg: String): Unit = dom.console.log(msg)
      override def warn(msg: String): Unit = dom.console.warn(msg)
      override def error(msg: String): Unit = dom.console.error(msg)
    }
    Injector.configure[Printer](printer)
    Injector.configure[JsonPrinter](printer)
  }

  def configure_execution_context()(implicit ec: ExecutionContext): Unit = {
    Injector.configure[ExecutionContext](ec)
  }

  def init_react(e: Event): Unit = {
    logger.info("Initializing application")
    val app_node = dom.document.getElementById(react_node)
    val router = Router(
      BaseUrl.fromWindowOrigin / "",
      RouterConfiguration(pages, layout).config
    )
    ReFormCSS.addToDocument()
    router().renderIntoDOM(app_node)

  }}
