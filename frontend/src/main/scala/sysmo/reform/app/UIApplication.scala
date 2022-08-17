package sysmo.reform.app

import japgolly.scalajs.react.extra.router.{BaseUrl, Router}
import org.scalajs.dom
import org.scalajs.dom.Event
import sysmo.reform.css.ReFormCSS
import sysmo.reform.util.log.Logging
import sysmo.reform.css.CssSettings._

trait UIApplication extends Logging {
  val react_node: String = "mainApp"
  def pages: PageCollection
  def app_config: Configuration
  val layout: Layout

  def main(args: Array[String]): Unit = {
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
