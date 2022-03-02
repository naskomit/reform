package sysmo.reform.components.forms

import scala.collection.mutable
import scala.concurrent.Future
import scala.reflect.ClassTag
import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, Observer, OverflowStrategy}
import monix.execution.Scheduler.Implicits.global
import monix.execution.cancelables.SingleAssignCancelable
import sysmo.reform.components.editors.{EditorAction, SelectEditor, StringEditor, ValueChanged}
import sysmo.reform.data.{RecordAction, UpdateField}
import sysmo.reform.components.actions.ActionHub
import sysmo.reform.data.{RecordAction, StreamingRecordManager, UpdateField}
import sysmo.reform.shared.data.{EnumeratedDomain, EnumeratedOption, Record, RecordField, RecordMeta, RecordWithMeta}


// TODO Handle premature component unmount
class StreamingFormEditor[U <: Record] {

  import japgolly.scalajs.react._
  import japgolly.scalajs.react.vdom.html_<^._

  type Props_ = StreamingFormEditor.Props[U]
  type State_ = StreamingFormEditor.State[U]

  final class Backend($: BackendScope[Props_, State_]) {
    println("Created StreamingFormEditor backend")

    private val state_observer = new Observer[U] {
      override def onNext(elem: U): Future[Ack] = {
        $.modState(s => s.copy(value = Some(elem))).runNow()
        Ack.Continue
      }

      override def onComplete(): Unit = {}

      override def onError(ex: Throwable): Unit = {}
    }

    private var state_subscription: Cancelable = _
    private var action_hub: ActionHub[EditorAction, RecordAction] = _

    def to_record_actions(key: String, editor_action: EditorAction): Seq[RecordAction] =
      editor_action match {
        case ValueChanged(v) => Seq(UpdateField("Form1", key, v))
        case _ => Seq()
      }

    def subscribe_to_records(p: Props_): Callback = Callback {
      state_subscription = p.rec_manager.record_stream.subscribe(state_observer)
      action_hub = ActionHub(p.meta.field_keys.map(_.toString), to_record_actions)
      action_hub.out.subscribe(p.rec_manager.action_handler)
    }

    def unsubscribe_from_records(): Callback = Callback {
      state_subscription.cancel()
    }

    // TODO cancel subscription on unmount

    def render(p: Props_, s: State_): VdomElement = {
      s.value match {
        case Some(data) => {
          val field_editors = p.meta.field_keys.map(k => {
            val RecordField(f_name, f_label, f_tpe, f_domain) = p.meta.fields(k)
            (f_tpe, f_domain) match {
              case (_, Some(EnumeratedDomain(options))) => {
//                val option_stream: Observable[Seq[EnumeratedOption]] = Observable.create(OverflowStrategy.Unbounded) {
//                  sub => sub.onNext(options)
//                  val c = SingleAssignCancelable()
//
//                  c := Cancelable(() => None)
//                }

                SelectEditor(
                  f_name, p.record_id, f_label,
                  p.meta.get_value(data, k).toString,
                  options,
                  action_hub.in_observers(k.toString)
                )

              }

              case _ =>
                StringEditor(
                  f_name, p.record_id, f_label,
                  p.meta.get_value(data, k).toString,
                  action_hub.in_observers(k.toString)
              )
            }
          })

          <.form(^.className := "form",
            p.layout.to_component(field_editors.map(x => x.vdomElement)))
        }

        case None => <.div("Loading form data")
      }

    }
  }

  implicit val props_reuse = Reusability.by((_ : Props_).record_id)
//
//  implicit val state_reuse = Reusability.by((_ : State_).value)

  val component =
    ScalaComponent.builder[Props_]("FormEditor")
      .initialState(StreamingFormEditor.State[U]())
      .backend(new Backend(_))
      .renderBackend
      .componentDidMount({
        println("FormEditor mounted")
        f => f.backend.subscribe_to_records(f.props)})
      .componentWillUnmount(f => f.backend.unsubscribe_from_records())
//      .configure(Reusability.shouldComponentUpdate)
      .build

}

object StreamingFormEditor {
  case class Props[U <: Record](record_id: String, layout: FormLayout, rec_manager: StreamingRecordManager[U], meta: RecordMeta[U])
  case class State[U <: Record](value: Option[U] = None,
                   state_subscription: Option[Cancelable] = None)


  val component_classes : mutable.Map[ClassTag[_], StreamingFormEditor[Record]] =
    mutable.Map()

  def get_component[U <: Record](tag: ClassTag[_]) =
    component_classes.getOrElseUpdate(tag, {
      println(f"Creating component for ${tag.runtimeClass}")
      (new StreamingFormEditor[U]).asInstanceOf[StreamingFormEditor[Record]]
    }).asInstanceOf[StreamingFormEditor[U]].component


  def apply[U <: Record](rec_manager: StreamingRecordManager[U], record_id: String, layout : FormLayout = ColumnarLayout(2))
                        (implicit meta_holder: RecordWithMeta[U], tag: ClassTag[U]) = {
    get_component[U](tag)(Props[U](record_id, layout, rec_manager, meta_holder._meta))
  }

}
