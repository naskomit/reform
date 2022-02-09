package sysmo.coviddata.components.forms

import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, Observer}
import sysmo.coviddata.components.editors.{EditorAction, StringEditor, ValueChanged}
import sysmo.coviddata.data.{AsyncFormDataInterface, RecordAction, StreamingRecordManager, UpdateField}
import sysmo.coviddata.shared.data
import sysmo.coviddata.shared.data.RecordMeta
import monix.execution.Scheduler.Implicits.global
import sysmo.coviddata.components.actions.ActionHub

import scala.concurrent.Future

// TODO Handle premature component unmount
object StreamingFormEditor {
  import japgolly.scalajs.react._
  import japgolly.scalajs.react.vdom.html_<^._

  case class Props[U](record_id: String, layout: FormLayout)
  case class State[U](value : Option[U] = None,
                      state_subscription: Option[Cancelable] = None)

  final class Backend[U]($: BackendScope[Props[U], State[U]])(rec_manager: StreamingRecordManager[U], meta: RecordMeta[U]) {

    private val state_observer = new Observer[U] {
      override def onNext(elem: U): Future[Ack] = {
        $.modState(s => s.copy(value = Some(elem))).runNow()
        Ack.Continue
      }

      override def onComplete(): Unit = {}

      override def onError(ex: Throwable): Unit = {}
    }

    private val state_subscription = rec_manager.record_stream.subscribe(state_observer)

    def subscribe_to_records() : Callback = Callback {

    }

    def unsubscribe_from_records() : Callback = Callback {
      state_subscription.cancel()
    }

    val action_hub = ActionHub(meta.field_keys.map(_.toString), to_record_actions)
    action_hub.out.subscribe(rec_manager.action_handler)
    // TODO cancel subscription on unmount

    def render (p: Props[U], s: State[U]): VdomElement = {
      s.value match {
        case Some(data) => {
          val field_editors = meta.field_keys.map(
            k => StringEditor(
              k.toString, p.record_id, meta.fields(k).label,
              meta.get_value(data, k).toString,
              action_hub.in_observers(k.toString)
            )
          )
          val k = meta.field_keys(0)
          <.form(^.className:="form", //field_editors.map(x => x.vdomElement))
            p.layout.to_component(field_editors.map(x => x.vdomElement)))
        }

        case None => <.div("Loading form data")
      }

    }
  }

  def component[U](rec_manager: StreamingRecordManager[U], meta: RecordMeta[U]) =
    ScalaComponent.builder[Props[U]]("FormEditor")
    .initialState(State[U]())
    .backend(new Backend[U](_)(rec_manager, meta))
    .renderBackend
    .componentDidMount(f => f.backend.subscribe_to_records())
    .componentWillUnmount(f => f.backend.unsubscribe_from_records())
    .build

  def to_record_actions(key: String, editor_action: EditorAction) : Seq[RecordAction] =
    editor_action match {
      case ValueChanged(v) => Seq(UpdateField("Form1", key, v))
      case _ => Seq()
    }

  def apply[U](rec_manager: StreamingRecordManager[U], record_id: String, layout : FormLayout = ColumnarLayout(2))(implicit meta_holder: data.RecordWithMeta[U]) = {
    println("Connstructed form")
    component[U](rec_manager, meta_holder._meta)(Props(record_id, layout))
  }

}

