package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.components.table.PredefinedTableViewer

object TablePanels {
  case class PanelProps(app_config: ApplicationConfiguration, table_id: String)

  val component = ScalaComponent.builder[PanelProps]("HomePage")
    .render_P(p => PredefinedTableViewer(p.app_config, p.table_id))
    .build

  object SocioDemographic extends ApplicationPanel {
    type Props = PanelProps
    type State = Unit
    type Backend = Unit
    def apply(app_config: ApplicationConfiguration) = {
      component(PanelProps(app_config, "SocioDemographic"))
    }
  }
  object Clinical extends ApplicationPanel {
    type Props = PanelProps
    type State = Unit
    type Backend = Unit
    def apply(app_config: ApplicationConfiguration) = {
      component(PanelProps(app_config, "Clinical"))
    }
  }
  object Therapy extends ApplicationPanel {
    type Props = PanelProps
    type State = Unit
    type Backend = Unit
    def apply(app_config: ApplicationConfiguration) = {
      component(PanelProps(app_config, "Therapy"))
    }
  }
  object Immunology extends ApplicationPanel {
    type Props = PanelProps
    type State = Unit
    type Backend = Unit
    def apply(app_config: ApplicationConfiguration) = {
      component(PanelProps(app_config, "Immunology"))
    }
  }
}
