package sysmo.reform.components.forms4

import japgolly.scalajs.react.BackendScope
import sysmo.reform.components.forms4.editors.{EditorAction, SetFieldValue}
import sysmo.reform.shared.data.{form4 => F}
import sysmo.reform.shared.data.form4.FieldEditor
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.util.log.Logging

import scala.concurrent.Future

trait FormDataHandler extends Logging {
  import FormDataHandler._
  val initial_data: F.ValueMap
  var state: State = State.Ready
  type BackendScopeType = BackendScope[FormEditorComponent.Props, FormEditorComponent.State]
  class Dispatcher($: BackendScopeType) extends EditorAction.Dispatcher {
    override def handle_action(action: EditorAction): Unit = {
//      action match {
//        case SetFieldValue(path, value) => $.modState(s => {
//          val new_state = s.copy(data = s.data.map(_.update(path, value)))
//          logger.debug(new_state.toString)
//          new_state
//        }).runNow()
//      }
    }
  }

  def get_value(editor: FieldEditor): F.FieldValue[_] = {
    logger.info(s"get_value: ${editor.path}")
    initial_data(editor.path)
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

  def get_choices(path: F.ElementPath, data: F.ValueMap): Future[Seq[LabeledValue[_]]]
}

object FormDataHandler {
  object State extends Enumeration {
    val Loading, Ready = Value
  }
  type State = State.Value

}