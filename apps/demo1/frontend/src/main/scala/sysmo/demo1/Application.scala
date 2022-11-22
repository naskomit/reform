package sysmo.demo1

import sysmo.reform.app.{PageCollection, SimplePage, UIApplication}
import sysmo.reform.layout.application.{ApplicationLayout, IntuitiveApplicationLayout}

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
    SimplePage("BioReactorController", None, "fa fa-list-check", BioReactorControllerPanel),
    SimplePage("SkullInventory", None, "fa fa-list-check", SkullInventoryPanel),
    SimplePage("SkullInventory2", None, "fa fa-list-check", SkullInventoryPanel2),
  )

  override val layout: ApplicationLayout = IntuitiveApplicationLayout
}
