package sysmo.reform.effects

import japgolly.scalajs.react.React
import japgolly.scalajs.react.React.Context
import sysmo.reform.shared.actions.{Action, ActionExecutor}

trait ActionManager {
  val parent: Option[ActionManager] = None
  def dispatch(action_program: Action): Unit
}

object DefaultActionManager extends ActionManager {
  val action_executor = ActionExecutor(
    Map(BrowserEffect.id -> new BrowserEffect.Handler)
  )

  def dispatch(action_program: Action): Unit = {
    println(action_program)
    action_executor.execute(action_program)
  }

}

object ActionManager {
  val ctx: Context[ActionManager] = React.createContext(DefaultActionManager)
}