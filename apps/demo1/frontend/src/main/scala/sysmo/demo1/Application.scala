package sysmo.demo1

import sysmo.reform.app.{Configuration, Layout, Page, PageCollection, Panel, UIApplication}
import sysmo.reform.layout.IntuitiveLayout

case object HomePage extends Page {
  override val name: String = "Home"
  override val icon: String = "fa fa-desktop"
  override val panel: Panel = HomePanel
}


object Application extends UIApplication {
  override def pages: PageCollection = PageCollection(
    HomePage
  )

  override def app_config: Configuration = new Configuration {

  }

  override val layout: Layout = IntuitiveLayout
}
