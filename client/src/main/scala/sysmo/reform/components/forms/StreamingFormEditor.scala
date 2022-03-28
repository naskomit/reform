package sysmo.reform.components.forms

import scala.collection.mutable
import scala.concurrent.Future
import scala.reflect.ClassTag
import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, Observer, OverflowStrategy}
import monix.execution.Scheduler.Implicits.global
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.editors.{AsyncSelectEditor, EditorAction, SelectEditor, StringEditor, ValueChanged}
import sysmo.reform.components.actions.ActionHub
import sysmo.reform.data.{RecordAction, StreamingRecordManager, UpdateField}
import sysmo.reform.shared.data.{EnumeratedDomain, EnumeratedDomainSource, EnumeratedOption, OptionFilter, OptionProvider, Record, RecordField, RecordMeta, RecordWithMeta}
import sysmo.reform.util.TypeSingleton


// TODO Handle premature component unmount
class StreamingFormEditor[U <: Record] extends ReactComponent {

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
            val field = p.meta.fields(k)
            val RecordField(f_name, f_label, f_tpe, f_domain) = field
            (f_tpe, f_domain) match {
              case (_, Some(EnumeratedDomainSource(option_provider, _))) => {

//                val field_option_provider = (flt: OptionFilter) => {
//                  option_provider.get(s.value, f_name, flt)
//                }

                AsyncSelectEditor(
                  field, p.record_id,
                  Seq(p.meta.get_value(data, k).toString),
                  action_hub.in_observers(k.toString)
                )

              }

              case _ =>
                StringEditor(
                  f_name, p.record_id, field.make_label,
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
        f => f.backend.subscribe_to_records(f.props)
      })
      .componentWillUnmount(f => f.backend.unsubscribe_from_records())
//      .configure(Reusability.shouldComponentUpdate)
      .build

}

object StreamingFormEditor extends TypeSingleton[StreamingFormEditor, Record] {
  case class Props[U <: Record](record_id: String, layout: FormLayout,
                                rec_manager: StreamingRecordManager[U],
                                meta: RecordMeta[U])
  case class State[U <: Record](value: Option[U] = None,
                   state_subscription: Option[Cancelable] = None)

  override def create_instance[U <: Record](implicit tag: ClassTag[U]): StreamingFormEditor[U] = new StreamingFormEditor[U]

  def apply[U <: Record](rec_manager: StreamingRecordManager[U], record_id: String,
                         meta: RecordMeta[U], layout : FormLayout = ColumnarLayout(2))
                        (implicit tag: ClassTag[U]) = {
    get_instance(tag).component(Props[U](record_id, layout, rec_manager, meta))
  }

}

