package sysmo.reform.components.forms4

import japgolly.scalajs.react.BackendScope
import sysmo.reform.components.forms4.editors.{EditorAction, RemoveArrayElement, SetFieldValue}
import sysmo.reform.shared.{form4, expr => E, form4 => F}
import sysmo.reform.shared.form4.{ElementPath, FieldEditor, FieldValue, FormElement, HandlerContext, LocalFieldIndex, ValueMap}
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
  class Dispatcher($: BackendScopeType) extends EditorAction.Dispatcher {
    override def handle_action(action: EditorAction): Unit = {
      action match {
        case SetFieldValue(path, value) => $.modState(s => {
          current_data = current_data.update(path, value)
          logger.debug(current_data.toString)
          s.copy(render_ind = s.render_ind + 1)
        }).runNow()

        case RemoveArrayElement(path, id) => $.modState(s => {
          val new_array_index: LocalFieldIndex = current_data.get(path) match {
            case LocalFieldIndex(ids) => form4.LocalFieldIndex(ids.filterNot(_ == id))
          }
          current_data = current_data
            .update(path, new_array_index)
            .remove(path / id)
          logger.debug(current_data.toString)
          s.copy(render_ind = s.render_ind + 1)

        }).runNow()

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

  def dispatch(action: EditorAction): Unit = {
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