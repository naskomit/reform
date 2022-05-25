package sysmo.reform.components.editors

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import monix.reactive.Observer
import sysmo.reform.components.ReactAction
import sysmo.reform.components.actions.ActionStreamGenerator
import sysmo.reform.components.select.ReactSelectFacades
import sysmo.reform.shared.{data => D}
import sysmo.reform.shared.data.form.{FieldOptionProvider, FieldValue, NoValue, SomeValue}
import sysmo.reform.shared.util.LabeledValue

import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._

object AsyncSelectEditor extends AbstractEditor {
  import ReactSelectFacades.{ReactSelectNativeComponent => RSNC}

  case class Props(field : D.Property, record_id: String, value : FieldValue,
                   action_listener: Observer[EditorAction], option_provider: FieldOptionProvider)
  case class State(choices: Seq[LabeledValue[_]]) //selection: fsm.Selection,

  final class Backend($: BackendScope[Props, State]) extends IBackend {
    val action_generator : ActionStreamGenerator[EditorAction] =
      ActionStreamGenerator[EditorAction]

//    case class Effects(props: Props) {
//    }

    def on_change(p: Props, s: State): RSNC.OnChange = (choice: RSNC.Choice, action_meta: RSNC.ActionMeta) => {
      action_meta.action match {
        case "select-option" => {
          val original_choice = s.choices.find(c => c.value == choice.value).get
          action_generator.dispatch(SetValue(SomeValue(original_choice)))
        }
        case "deselect-option" => throw new IllegalArgumentException("deselect-option")
        case "remove-value" => throw new IllegalArgumentException("remove-value")
        case "pop-value" => throw new IllegalArgumentException("pop-value")
        case "clear" => action_generator.dispatch(SetValue(NoValue))
        case "create-option" => throw new IllegalArgumentException("create-option")
      }
    }

    def on_input_change(p: Props, s: State): RSNC.OnInputChange = (value: String, action_meta: RSNC.ActionMeta) => {
//      dom.console.log(action_meta)
//      println(value, action_meta.action)
    }

    def on_menu_open(p: Props, s: State): RSNC.OnMenuOpen = () => {
      p.option_provider.get(D.NoFilter)
        .map(choices => {
          $.modState(s => s.copy(choices = choices)).runNow()
        })
      ()
    }

    def render(p: Props, s: State) : VdomElement = {
      <.div(^.className:= "form-group", ^.key:= p.field.name,
        <.label(p.field.label),
        RSNC(
          p.value match {
            case SomeValue(x) => Some(x)
            case NoValue => None
          },
          s.choices,
          on_change = Some(on_change(p,s)),
          on_input_change = Some(on_input_change(p, s)),
          on_menu_open = Some(on_menu_open(p, s)),
          is_clearable = Some(true)
        )
      )
    }


    override def handle_action(props: Props, state: State)(action: ReactAction): AsyncSelectEditor.AsyncCallback[Unit] = ???
  }

  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  implicit val props_reuse = Reusability.by((_ : Props).value)
  implicit val choices_reuse = Reusability.by_==[Seq[LabeledValue[_]]]
  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props]("SelectEditor")
    .initialState(State(Seq())) //fsm.SelectedNone
    .renderBackend[Backend]
    .componentDidMount(f => Callback {
          f.backend.action_generator.start(f.props.action_listener)
    })
//    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(field : D.Property, record_id: String, value : FieldValue,
            action_listener: Observer[EditorAction], option_provider: FieldOptionProvider) =
    component(Props(field, record_id, value, action_listener, option_provider))
}
