package sysmo.demo1

import sysmo.reform.app.{Configuration, Layout, Page, PageCollection, Panel, SimplePage, UIApplication}
import sysmo.reform.layout.IntuitiveLayout

//case object HomePage extends Page {
//  override val name: String = "Home"
//  override val icon: String = "fa fa-desktop"
//  override val panel: Panel = HomePanel
//}

// "fa fa-list-check"
// "fa fa-building"
// "fa fa-bong"
// "fa fa-book-medical"
// "fa fa-shuttle-space"
object Application extends UIApplication {
  override def pages: PageCollection = PageCollection(
//    HomePage,
    SimplePage("RuntimeExplorer", None, "fa fa-list-check", RuntimeExplorerPanel),
  )

  override def app_config: Configuration = new Configuration {

  }

  override val layout: Layout = IntuitiveLayout
}
