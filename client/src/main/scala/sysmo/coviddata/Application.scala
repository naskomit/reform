package sysmo.coviddata


import router.Pages
import sysmo.reform.ReactApplication
import sysmo.reform.components.layouts.IntuitiveLayout
import org.scalajs.dom.Event
import sysmo.reform.shared.data.{table => sdt}

object Application extends ReactApplication {
  val react_node = "mainApp"
  val pages = Pages.collection
  val layout = IntuitiveLayout

  override def init_react(e: Event): Unit = {
    sdt.enable_global_manager(true)
    super.init_react(e)
  }

}
