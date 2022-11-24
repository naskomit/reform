package sysmo.reform.effects

import japgolly.scalajs.react.{Callback, React}
import japgolly.scalajs.react.React.Context
import cats.syntax.all._
import sysmo.reform.shared.actions.{Action, ActionExecutor, Actionable, SideEffect}
import sysmo.reform.shared.logging.Logging

trait ActionManager extends Logging {
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
      .handleError(error => logger.error(error))
  }

}

object ActionManager {
  val ctx: Context[ActionManager] = React.createContext(DefaultActionManager)
}