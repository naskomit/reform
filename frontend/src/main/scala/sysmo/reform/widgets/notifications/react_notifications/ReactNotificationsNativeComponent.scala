package sysmo.reform.widgets.notifications.react_notifications

import japgolly.scalajs.react.{Children, JsComponent}

import scala.scalajs.js

object ReactNotificationsNativeComponent {
  @js.native
  trait Props extends js.Object {

  }

  val component = JsComponent[Props, Children.None, Null](ReactNotificationsFacade.NotificationContainer)

  def apply() = {
    val p = js.Object.asInstanceOf[Props]
    component.withProps(p).apply()
  }
}
