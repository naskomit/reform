package sysmo.reform.components.forms4

import japgolly.scalajs.react.BackendScope
import sysmo.reform.components.forms4.editors.{EditorAction, SetFieldValue}
import sysmo.reform.shared.data.{form4 => F}
import sysmo.reform.shared.data.form4.{FieldEditor, GraphObject}
import sysmo.reform.shared.gremlin.tplight.Graph
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.util.log.Logging

import scala.concurrent.Future

abstract class FormDataHandler(_graph: Graph) extends GraphObject with Logging {
  import FormDataHandler._
  def initial_data: F.ValueMap
  override def graph: Graph = _graph
  var handler_state: HandlerState = HandlerState.Ready

  var current_data: F.ValueMap = initial_data
  type BackendScopeType = BackendScope[FormEditorComponent.Props, FormEditorComponent.State]
  class Dispatcher($: BackendScopeType) extends EditorAction.Dispatcher {
    override def handle_action(action: EditorAction): Unit = {
      action match {
        case SetFieldValue(path, value) => $.modState(s => {
          current_data = current_data.update(path, value)
          logger.debug(current_data.toString)
          s.copy(render_ind = s.render_ind + 1)
        }).runNow()
      }
    }
  }

  def get_value(editor: FieldEditor): F.FieldValue[_] = {
    current_data(editor.path)
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

  def get_choices(element: F.FormElement): Future[Seq[LabeledValue[_]]]
}

object FormDataHandler {
  object HandlerState extends Enumeration {
    val Loading, Ready = Value
  }
  type HandlerState = HandlerState.Value

}