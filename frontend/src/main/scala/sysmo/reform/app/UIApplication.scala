package sysmo.reform.app

import io.circe.Json
import japgolly.scalajs.react.extra.router.{BaseUrl, Router}
import org.scalajs.dom
import org.scalajs.dom.Event
import sysmo.reform.css.ReFormCSS
import sysmo.reform.shared.logging.{JsonPrinter, Logging, Printer}
import sysmo.reform.css.CssSettings._
import sysmo.reform.shared.util.Injector

trait UIApplication extends Logging {
  val react_node: String = "mainApp"
  def pages: PageCollection
  def app_config: Configuration
  val layout: Layout

  def main(args: Array[String]): Unit = {
    val printer = new JsonPrinter {
      override def out(msg: Json): Unit = dom.console.log(msg)
      override def warn(msg: Json): Unit = dom.console.warn(msg)
      override def error(msg: Json): Unit = dom.console.error(msg)
      override def out(msg: String): Unit = dom.console.log(msg)
      override def warn(msg: String): Unit = dom.console.warn(msg)
      override def error(msg: String): Unit = dom.console.error(msg)
    }
    Injector.configure[Printer](printer)
    Injector.configure[JsonPrinter](printer)
    dom.window.addEventListener("load", init_react)
  }

  def init_react(e: Event): Unit = {
    logger.info("Initializing application")
    val app_node = dom.document.getElementById(react_node)
    val router = Router(
      BaseUrl.fromWindowOrigin / "",
      RouterConfiguration(pages, app_config, layout).config
    )
    ReFormCSS.addToDocument()
    router().renderIntoDOM(app_node)

  }}
