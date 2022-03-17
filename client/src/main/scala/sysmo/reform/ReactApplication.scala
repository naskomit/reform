package sysmo.reform

import org.scalajs.dom
import org.scalajs.dom.Event
import japgolly.scalajs.react.extra.router.{BaseUrl, Router}
import sysmo.reform.components.layouts.ApplicationLayout
import sysmo.reform.router.{PageCollection, RouterConfiguration}

trait ReactApplication {
  val react_node: String
  val pages: PageCollection
  val layout: ApplicationLayout

  def main(args: Array[String]): Unit = {
    dom.window.addEventListener("load", init_react)
  }

  def init_react(e: Event): Unit = {
    println("Initializing application")
    val app_node = dom.document.getElementById(react_node)
    val router = Router(
      BaseUrl.fromWindowOrigin / "",
      RouterConfiguration(pages, layout).config
    )
    router().renderIntoDOM(app_node)

  }

}
