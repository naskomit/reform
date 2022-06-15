package sysmo.reform.components.forms

import japgolly.scalajs.react.BackendScope
import sysmo.reform.components.forms.{editors => Edit}
import sysmo.reform.shared.{expr => E, form => F}
import sysmo.reform.shared.form.{ElementPath, FieldEditor, FieldValue, FormElement, HandlerContext, LocalFieldIndex, ValueMap}
import sysmo.reform.shared.gremlin.tplight.Graph
import sysmo.reform.shared.gremlin.tplight.gobject.GraphObject
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.util.log.Logging

import scala.concurrent.Future

abstract class FormDataHandler(_graph: Graph) extends GraphObject with Logging {
  import FormDataHandler._
  def initial_data: ValueMap
  override def graph: Graph = _graph
  var handler_state: HandlerState = HandlerState.Ready

  var current_data: ValueMap = initial_data
  type BackendScopeType = BackendScope[FormEditorComponent.Props, FormEditorComponent.State]
  class Dispatcher($: BackendScopeType) extends Edit.EditorAction.Dispatcher {
    override def handle_action(action: Edit.EditorAction): Unit = {
      action match {
        case Edit.SetFieldValue(path, value) => $.modState(s => {
          current_data = current_data.update(path, value)
          logger.debug(current_data.toString)
          s.copy(render_ind = s.render_ind + 1)
        }).runNow()

        case Edit.RemoveArrayElement(array, id) => $.modState(s => {
          current_data = array.remove_element(current_data, id)
          logger.debug(current_data.toString)
          s.copy(render_ind = s.render_ind + 1)

        }).runNow()

        case Edit.InsertElementBefore(array, id) => $.modState(s => {
          current_data = array.insert_array_element(current_data, id, before = true)
          s.copy(render_ind = s.render_ind + 1)
        }).runNow()

        case Edit.InsertElementAfter(array, id) => $.modState(s => {
          current_data = array.insert_array_element(current_data, id, before = false)
          s.copy(render_ind = s.render_ind + 1)
        }).runNow()

        case Edit.AppendElement(array) => $.modState(s => {
          current_data = array.append_element(current_data)
          s.copy(render_ind = s.render_ind + 1)
        }).runNow

        case _ => logger.error(s"Unknow action ${action}")
      }
    }
  }

  def context(base: FormElement): HandlerContext =
    new HandlerContext(base, current_data)

  def get_value(path: ElementPath): FieldValue[_] = current_data.get(path)
  def get_value(editor: FieldEditor): FieldValue[_] = current_data.get(editor.path)

//  def get_array(array: F.GroupArray): Seq[F.ElementPath] = {
//    val array_index = current_data.get(array.path).match {
//      case F.MultiValue(v) => v.map {
//        case LabeledValue(x, _) => x.asInstanceOf[String]
//      }
//      case _ => {
//        logger.warn(s"Perhaps not array at ${array.path}")
//        Seq()
//      }
//    }
//    array_index.map(uid => current_data.get(array.path / uid)
//  }

  protected var dispatcher: Option[Dispatcher] = None

  def bind($: BackendScopeType): Unit = {
    dispatcher match {
      case None => dispatcher = Some(new Dispatcher($))
      case Some(_) => logger.error("FormDataHandler already bound to form component!")
    }
  }

  def unbind($: BackendScopeType): Unit = {
    dispatcher match {
      case Some(_) => dispatcher = None
      case None => logger.warn("FormDataHandler not bound to form component!")
    }
  }

  def dispatch(action: Edit.EditorAction): Unit = {
    dispatcher match {
      case None => logger.error("FormDataHandler not bound to form!")
      case Some(dsp) => dsp.dispatch(action)
    }
  }

  def get_choices(element: FormElement): Future[Seq[LabeledValue[_]]]
}

object FormDataHandler {
  object HandlerState extends Enumeration {
    val Loading, Ready = Value
  }
  type HandlerState = HandlerState.Value

}