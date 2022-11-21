package sysmo.reform.app

import japgolly.scalajs.react.React
import japgolly.scalajs.react.React.Context
import org.scalajs.dom
import sysmo.reform.react.ReactComponent

trait PanelManager {
  def toast(msg: String): Unit
}

object DefaultPanelManager extends PanelManager {
  override def toast(msg: String): Unit = {
    dom.console.log(s"Panel Manager: $msg")
  }
}

object PanelManager {
  val ctx: Context[PanelManager] = React.createContext(DefaultPanelManager)
}

trait Panel extends ReactComponent{
  def apply(app_config: Configuration): Unmounted
}
