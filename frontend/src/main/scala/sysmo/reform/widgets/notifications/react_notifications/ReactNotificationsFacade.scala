package sysmo.reform.widgets.notifications.react_notifications

import org.scalajs.dom
import sysmo.reform.shared.logging.Logging

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object ReactNotificationsFacade extends Logging {

  @JSImport("react-notifications", "NotificationManager")
  @js.native
  object NotificationManager extends js.Object {
    def info(msg: String): Unit = js.native
    def success(msg: String): Unit = js.native
    def warning(msg: String): Unit = js.native
    def error(msg: String): Unit = js.native
  }

  @JSImport("react-notifications", "NotificationContainer")
  @js.native
  object NotificationContainer extends js.Object

  def hello() = {
    println("ToastContainer")
    dom.console.log(NotificationManager)
  }
//  @JSImport("react-toastify", JSImport.Namespace)
//  @js.native
//  object ReactToastify extends js.Object {
//    val toast: js.Function1[String, Unit] = js.native
//  }

}
