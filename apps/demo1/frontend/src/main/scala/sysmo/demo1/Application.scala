package sysmo.demo1

import sysmo.reform.app.{PageCollection, SimplePage, UIApplication}
import sysmo.reform.layout.application.{ApplicationLayout, IntuitiveApplicationLayout}

object Application extends UIApplication {
  override def pages(app: Option[String]): PageCollection = app match {
    case Some("metadata") => SkullInventoryApplication.pages
    case _ => HomeApplication.pages
  }

  override val layout: ApplicationLayout = IntuitiveApplicationLayout
}

object HomeApplication {
  val pages: PageCollection = PageCollection(
    SimplePage("Home", Some("Home"), "fa fa-list-check", VACHomePanel),
  )
}

object SkullInventoryApplication {
  val pages: PageCollection = PageCollection(
    SimplePage("inventory_metadata", Some("Inventory Metadata"), "fa fa-list-check", InventoryMetadata),
    SimplePage("project_info", Some("Project Information"), "fa fa-list-check", InventoryProjectInfoPanel),
  )
}
