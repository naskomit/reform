package sysmo.reform.components.forms3

import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.data.{form3 => F}
import F.{FormData => FD}
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.callback.Callback
import sysmo.reform.components.forms3.editors.{EditorAction, SetFieldValue}
import sysmo.reform.util.log.Logging

import scala.concurrent.Future

trait FormDataHandler extends Logging {
  val initial_data: FD.ValueMap
  type BackendScopeType = BackendScope[FormEditorComponent.Props, FormEditorComponent.State]
  class Dispatcher($: BackendScopeType) extends EditorAction.Dispatcher {
    override def handle_action(action: EditorAction): Unit = {
      action match {
        case SetFieldValue(path, value) => $.modState(s => {
          val new_state = s.copy(data = s.data.map(_.update(path, value)))
          logger.debug(new_state.toString)
          new_state
        }).runNow()
      }
    }
  }
  protected var dispatcher: Option[Dispatcher] = None

  def bind($: BackendScopeType): Unit = {
    dispatcher match {
      case None => dispatcher = Some(new Dispatcher($))
      case Some(_) => logger.error("FormDataHandler already bound to form component!")
    }
  }

  def dispatch(action: EditorAction): Unit = {
    dispatcher match {
      case None => logger.error("FormDataHandler not bound to form!")
      case Some(dsp) => dsp.dispatch(action)
    }
  }

  def get_choices(path: F.ElementPath, data: FD.ValueMap): Future[Seq[LabeledValue[_]]]
}