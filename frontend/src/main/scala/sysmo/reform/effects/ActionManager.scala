package sysmo.reform.effects

import japgolly.scalajs.react.{Callback, React}
import japgolly.scalajs.react.React.Context
import sysmo.reform.shared.actions.{Action, ActionExecutor, Actionable, SideEffect}

trait ActionManager {
  val parent: Option[ActionManager] = None
  def dispatch(action: Actionable): Unit
  def callback(action: Actionable): Callback = Callback {
    dispatch(action)
  }
}

object DefaultActionManager extends ActionManager {
  val action_executor = ActionExecutor(
    Map(BrowserEffect.id -> new BrowserEffect.Handler)
  )

  def dispatch(action: Actionable): Unit = {
    println(action)
    action_executor.execute(action.as_action)
  }

}

object ActionManager {
  val ctx: Context[ActionManager] = React.createContext(DefaultActionManager)
}