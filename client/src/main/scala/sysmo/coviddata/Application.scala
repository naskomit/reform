package sysmo.coviddata

import org.scalajs.dom
import org.scalajs.dom.Event
import japgolly.scalajs.react.extra.router.{BaseUrl, Router}
import router.RouterConfiguration
import sysmo.coviddata.services.DataApiClient
import sysmo.coviddata.shared.data.PatientData
import autowire._
import scalajs.concurrent.JSExecutionContext.Implicits.queue

object Application {
    def main(args: Array[String]): Unit = {
      dom.window.addEventListener("load", initApp)
    }

  def initApp(e: Event): Unit = {
    println("Initializing application")
    val a = DataApiClient[PatientData].list_patients().call().foreach(println)
    val app_node = dom.document.getElementById("mainApp")

//    HomePanel().renderIntoDOM(app_node)
    val router = Router(BaseUrl.fromWindowOrigin / "", RouterConfiguration.config)
    router().renderIntoDOM(app_node)
  }
}
