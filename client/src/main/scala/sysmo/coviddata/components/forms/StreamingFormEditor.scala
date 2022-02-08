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

  case class Props[U](record_id: String, meta: RecordMeta[U], layout: FormLayout,
                      action_hub: ActionHub[EditorAction, RecordAction])
  case class State[U](value : Option[U] = None,
                      state_subscription: Option[Cancelable] = None)

  final class Backend[U]($: BackendScope[Props[U], State[U]]) {
    val state_observer = new Observer[U] {
      override def onNext(elem: U): Future[Ack] = {
        $.modState(s => s.copy(value = Some(elem))).runNow()
        Ack.Continue
      }

      override def onComplete(): Unit = {}

      override def onError(ex: Throwable): Unit = {}
    }
    //val subscription = record_stream.subscribe(state_observer)

    def subscribe_to_records(record_stream : Observable[U]) : Callback = {
      $.modState(s => {
        val subscription = record_stream.subscribe(state_observer)
        s.copy(state_subscription = Some(subscription))
      })
    }

    def unsubscribe_from_records(s: State[U]) : Callback = Callback {
      s.state_subscription match {
        case Some(x) => x.cancel()
        case None =>
      }
    }

    def render (p: Props[U], s: State[U]): VdomElement = {
      s.value match {
        case Some(data) => {
          val field_editors = p.meta.field_keys.map(
            k => StringEditor(
              k.toString, p.record_id, p.meta.fields(k).label,
              p.meta.get_value(data, k).toString,
              p.action_hub.in_observers(k.toString)
            )
          )
          val k = p.meta.field_keys(0)
          <.form(^.className:="form", //field_editors.map(x => x.vdomElement))
            p.layout.to_component(field_editors.map(x => x.vdomElement)))
        }

        case None => <.div("Loading form data")
      }

    }
  }

  def component[U](record_stream : Observable[U]) = ScalaComponent.builder[Props[U]]("FormEditor")
    .initialState(State[U]())
    .backend(new Backend[U](_))
    .renderBackend
    .componentDidMount(f => f.backend.subscribe_to_records(record_stream))
    .componentWillUnmount(f => f.backend.unsubscribe_from_records(f.state))
    .build

  def to_record_actions(key: String, editor_action: EditorAction) : Seq[RecordAction] =
    editor_action match {
      case ValueChanged(v) => Seq(UpdateField("Form1", key, v))
      case _ => Seq()
    }

  def apply[U](rec_manager: StreamingRecordManager[U], record_id: String, layout : FormLayout = ColumnarLayout(2))(implicit meta_holder: data.RecordWithMeta[U]) = {
    println("Connstructed form")
    val meta = meta_holder._meta
    val action_hub = ActionHub(meta.field_keys.map(_.toString), to_record_actions)
    // TODO cancel subscription on unmount
    action_hub.out.subscribe(rec_manager.action_handler)
    component[U](rec_manager.record_stream)(Props(record_id, meta, layout, action_hub))
  }

}

