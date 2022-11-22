package sysmo.reform.widgets.notifications

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.VdomElement
import sysmo.reform.widgets.ReactComponent
import sysmo.reform.widgets.notifications.react_notifications.{ReactNotificationsFacade, ReactNotificationsNativeComponent}

object ToastNotifications extends ReactComponent{

  case class Props()
  case class State()

  final class  Backend($: BScope) {
    def render(p: Props, s: State): VdomElement = {
      ReactNotificationsNativeComponent()
    }
  }

  val component = ScalaComponent.builder[Props]("ToastNotifications")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(): Unmounted = component(Props())

  private val manager = ReactNotificationsFacade.NotificationManager
  def info(msg: String): Unit = manager.info(msg)
  def success(msg: String): Unit = manager.success(msg)
  def warning(msg: String): Unit = manager.warning(msg)
  def error(msg: String): Unit = manager.error(msg)

}
