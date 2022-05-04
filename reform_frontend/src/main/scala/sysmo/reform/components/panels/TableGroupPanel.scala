package sysmo.reform.components.panels

import japgolly.scalajs.react._
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.components.table.PredefinedTableViewer
import sysmo.reform.router.{Category, Page, PageBase}
import sysmo.reform.shared.data.{table => sdt}

import scala.collection.mutable

case class TableGroupPanel(name: String, label: Option[String], icon: String, children: Seq[PageBase])
  extends Category

object TableGroupPanel {
  case class PanelProps(app_config: ApplicationConfiguration, table_id: String, table_label: Option[String])
  val component = ScalaComponent.builder[PanelProps]("TablePanels")
    .render_P(p => PredefinedTableViewer(p.app_config, p.table_id, p.table_label))
    .build

  class Builder(_name: String, _label: Option[String]) {
    var _icon: String = "fa fa-database"
    var _children = mutable.ArrayBuffer[PageBase]()
    def add(schema: sdt.Schema): this.type = {
      val page = new Page {
        val name = schema.name
        val label = schema.label
        val icon = "fa-database"
        val panel: ApplicationPanel = new ApplicationPanel {
          type Props = PanelProps
          type State = Unit
          type Backend = Unit
          def apply(app_config: ApplicationConfiguration) = {
            component(PanelProps(app_config, schema.name, schema.label))
          }
        }
      }
      _children += page
      this
    }

    def build: Category = new TableGroupPanel(_name, _label, _icon, _children.toSeq)
  }
  def builder(name: String, label: Option[String] = None) = new Builder(name, label)
}