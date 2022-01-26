package sysmo.coviddata

import org.scalajs.dom
import org.scalajs.dom.Event
import japgolly.scalajs.react.extra.router.{Router, BaseUrl}
import router.RouterConfiguration


object Application {
    def main(args: Array[String]): Unit = {
      dom.window.addEventListener("load", initApp)
    }

  def initApp(e: Event): Unit = {
    println("Initializing application")
    val app_node = dom.document.getElementById("mainApp")

//    HomePanel().renderIntoDOM(app_node)
    val router = Router(BaseUrl.fromWindowOrigin / "", RouterConfiguration.config)
    router().renderIntoDOM(app_node)
  }
}
