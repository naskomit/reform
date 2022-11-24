package sysmo.reform.effects

import cats.MonadThrow
import sysmo.reform.shared.actions.{EffectHandler, SideEffect}
import sysmo.reform.shared.data.{ObjectId, StringObjectId, Value}
import Value.implicits._
import japgolly.scalajs.react.facade
import org.scalajs.dom
import sysmo.reform.widgets.notifications.ToastNotifications
import japgolly.scalajs.react.vdom.VdomElement
import sysmo.reform.widgets.tooltip.TooltipViewer

sealed trait BrowserEffect extends SideEffect {
  override def group: ObjectId = BrowserEffect.id
}

case class CopyToClipboard(v: Value) extends BrowserEffect

sealed trait Notify extends BrowserEffect
case class NotifyInfo(msg: String) extends Notify
case class NotifySuccess(msg: String) extends Notify
case class NotifyWarning(msg: String) extends Notify
case class NotifyError(msg: String) extends Notify

sealed trait TooltipEffect extends BrowserEffect
case class ShowTooltip(x: Double, y: Double, content: facade.React.Node) extends TooltipEffect
case object HideTooltip extends TooltipEffect

object BrowserEffect {
  val id = StringObjectId("BrowserEffect")
  class Handler[F[+_]](implicit val mt: MonadThrow[F])
    extends EffectHandler[F] {
    type T = BrowserEffect
    override def handle_effect(action: BrowserEffect): F[Unit] = action match {
      case CopyToClipboard(v) => {
        v.get[String].foreach(v_text =>
          dom.window.navigator.clipboard.writeText(v_text)
        )
        mt.pure()
      }
      case action: Notify => {
        action match {
          case NotifyInfo(msg) => ToastNotifications.info(msg)
          case NotifySuccess(msg) => ToastNotifications.success(msg)
          case NotifyWarning(msg) => ToastNotifications.warning(msg)
          case NotifyError(msg) => ToastNotifications.error(msg)
        }
        mt.pure()
      }
      case action: TooltipEffect => {
        action match {
          case ShowTooltip(x, y, content) => TooltipViewer.show(x, y, content)
          case HideTooltip => TooltipViewer.hide()
        }
        mt.pure()
      }
    }
  }
}





