package sysmo.coviddata

import org.scalajs.dom
import org.scalajs.dom.Event
import japgolly.scalajs.react.extra.router.{BaseUrl, Router}
import router.RouterConfiguration

object Application {
    def main(args: Array[String]): Unit = {
      dom.window.addEventListener("load", initApp)
    }

  def initApp(e: Event): Unit = {
    println("Initializing application")
    val app_node = dom.document.getElementById("mainApp")

    val router = Router(BaseUrl.fromWindowOrigin / "", RouterConfiguration.config)
    router().renderIntoDOM(app_node)
  }
}
