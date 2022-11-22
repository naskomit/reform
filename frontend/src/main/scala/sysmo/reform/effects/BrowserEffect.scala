package sysmo.reform.effects

import cats.MonadThrow
import sysmo.reform.shared.actions.{SideEffect, EffectHandler}
import sysmo.reform.shared.data.{ObjectId, StringObjectId, Value}
import Value.implicits._
import org.scalajs.dom
import sysmo.reform.widgets.notifications.react_notifications.ReactNotificationsFacade.NotificationManager


sealed trait BrowserEffect extends SideEffect {
  override def group: ObjectId = BrowserEffect.id
}

case class CopyToClipboard(v: Value) extends BrowserEffect
sealed trait Notify extends BrowserEffect
case class NotifyInfo(msg: String) extends Notify
case class NotifySuccess(msg: String) extends Notify
case class NotifyWarning(msg: String) extends Notify
case class NotifyError(msg: String) extends Notify

object BrowserEffect {
  val id = StringObjectId("BrowserEffect")
  class Handler[F[+_]](implicit val mt: MonadThrow[F])
    extends EffectHandler[F] {
    type T = BrowserEffect
    override def handle_effect(action: BrowserEffect): F[Unit] = action match {
      case CopyToClipboard(v) => {
        v.get[String].foreach(v_text => dom.window.navigator.clipboard.writeText(v_text))
        mt.pure()
      }
      case action: Notify => {
        action match {
          case NotifyInfo(msg) => NotificationManager.info(msg)
          case NotifySuccess(msg) => NotificationManager.success(msg)
          case NotifyWarning(msg) => NotificationManager.warning(msg)
          case NotifyError(msg) => NotificationManager.error(msg)
        }
        mt.pure()
      }
    }
  }
}





