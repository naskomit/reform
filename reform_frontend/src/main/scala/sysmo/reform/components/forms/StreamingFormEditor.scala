package sysmo.reform.components.forms

import scala.collection.mutable
import scala.concurrent.Future
import scala.reflect.ClassTag
import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, Observer, OverflowStrategy}
import monix.execution.Scheduler.Implicits.global
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.editors.{AsyncSelectEditor, StringEditor}
import sysmo.reform.components.actions.ActionHub
import sysmo.reform.components.editors.{EditorAction, UpdateValue}
import sysmo.reform.data.{RecordAction, StreamingRecordManager, UpdateField}
import sysmo.reform.shared.{data => D}
import sysmo.reform.shared.data.form.{FieldOptionProvider, Record, RecordMeta}
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.util.TypeSingleton


// TODO Handle premature component unmount
object FormEditorModeFSM {
  sealed trait FormEditorMode
  case object Uninitialized extends FormEditorMode
  case object Initialized extends FormEditorMode
}

class StreamingFormEditor[U <: Record] extends ReactComponent {

  import japgolly.scalajs.react._
  import japgolly.scalajs.react.vdom.html_<^._
  import sysmo.reform.components.forms.{FormEditorModeFSM => fsm}

  type Props_ = StreamingFormEditor.Props[U]
  type State_ = StreamingFormEditor.State


  final class Backend($: BackendScope[Props_, State_]) {
    private val state_observer = new Observer[Record.ValueMap] {
      override def onNext(elem: Record.ValueMap): Future[Ack] = {
        $.modState(s => s.copy(value = elem)).runNow()
        Ack.Continue
      }

      override def onComplete(): Unit = {}

      override def onError(ex: Throwable): Unit = {}
    }

    private var state_subscription: Cancelable = _
    private var action_hub: ActionHub[EditorAction, RecordAction] = _

    def to_record_actions(key: String, editor_action: EditorAction): Seq[RecordAction] =
      editor_action match {
        case a : UpdateValue => Seq(UpdateField("Form1", key, a))
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

    def render(p: Props_, s: State_): VdomElement = {
      s.mode match {
        case fsm.Uninitialized => <.div("Initializing")
        case fsm.Initialized => {
          val field_editors = p.meta.field_keys.map(k => {
            val field = p.meta.fields(k)
            val D.Property(f_name, f_label, f_tpe, f_mult, f_domain) = field
            (f_tpe, f_domain) match {
              case (_, Some(D.CategoricalDomainSource(_, _))) => {
                AsyncSelectEditor(
                  field, p.record_id,
                  s.value(field.name),
                  action_hub.in_observers(k.toString),
                  new FieldOptionProvider {
                    override def get(flt: D.OptionFilter): Future[Seq[LabeledValue[_]]] =
                      p.meta.option_provider.get_options(s.value, field.name, flt)
                  }
                )

              }

              case _ =>
                StringEditor(
                  f_name, p.record_id, field.make_label,
                  s.value(field.name),
                  action_hub.in_observers(k.toString)
                )
            }
          })

          <.form(^.className := "form",
            p.layout.to_component(field_editors.map(x => x.vdomElement)))
        }
      }
    }
  }

  implicit val props_reuse = Reusability.by((_ : Props_).record_id)
//
//  implicit val state_reuse = Reusability.by((_ : State_).value)

  val component =
    ScalaComponent.builder[Props_]("FormEditor")
      .initialStateFromProps(p => StreamingFormEditor.State(p.rec_manager.state, mode = fsm.Uninitialized))
      .backend(new Backend(_))
      .renderBackend
      .componentDidMount({
        f => {
          f.backend.subscribe_to_records(f.props) >> f.modState(s => s.copy(mode = fsm.Initialized))
        }
      })
      .componentWillUnmount(f => f.backend.unsubscribe_from_records())
//      .configure(Reusability.shouldComponentUpdate)
      .build

}

object StreamingFormEditor extends TypeSingleton[StreamingFormEditor, Record] {
  import sysmo.reform.components.forms.{FormEditorModeFSM => fsm}

  case class Props[U <: Record](record_id: String, layout: FormLayout,
                                rec_manager: StreamingRecordManager[U],
                                meta: RecordMeta[U])
  case class State(value: Record.ValueMap,
                   state_subscription: Option[Cancelable] = None,
                   mode: fsm.FormEditorMode)

  override def create_instance[U <: Record](implicit tag: ClassTag[U]): StreamingFormEditor[U] = new StreamingFormEditor[U]

  def apply[U <: Record](rec_manager: StreamingRecordManager[U], record_id: String,
                         meta: RecordMeta[U], layout : FormLayout = ColumnarLayout(2))
                        (implicit tag: ClassTag[U]) = {
    get_instance(tag).component(Props[U](record_id, layout, rec_manager, meta))
  }

}

