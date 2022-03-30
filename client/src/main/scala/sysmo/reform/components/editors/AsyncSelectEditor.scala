package sysmo.reform.components.editors

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import monix.reactive.Observer
import sysmo.reform.components.ReactAction
import sysmo.reform.components.actions.ActionStreamGenerator
import sysmo.reform.components.select.ReactSelectFacades
import sysmo.reform.shared.data.{EnumeratedOption, FieldOptionProvider, FieldValue, NoFilter, NoValue, OptionFilter, RecordOptionProvider, RecordField, SomeValue}

import scalajs.concurrent.JSExecutionContext.Implicits.queue

object AsyncSelectEditor extends AbstractEditor {
  import ReactSelectFacades.{ReactSelectNativeComponent => RSNC}

  case class Props(field : RecordField, record_id: String, value : FieldValue[String],
                   action_listener: Observer[EditorAction], option_provider: FieldOptionProvider)
  case class State(choices: Seq[EnumeratedOption]) //selection: fsm.Selection,

  final class Backend($: BackendScope[Props, State]) extends IBackend {
    val action_generator : ActionStreamGenerator[EditorAction] =
      ActionStreamGenerator[EditorAction]

//    case class Effects(props: Props) {
//    }

    def on_change(p: Props, s: State): RSNC.OnChange = (choice: RSNC.Choice, action_meta: RSNC.ActionMeta) => {
      action_meta.action match {
        case "select-option" => action_generator.dispatch(SetValue(SomeValue(choice.value)))
        case "deselect-option" => throw new IllegalArgumentException("deselect-option")
        case "remove-value" => throw new IllegalArgumentException("remove-value")
        case "pop-value" => throw new IllegalArgumentException("pop-value")
        case "clear" => action_generator.dispatch(SetValue(NoValue[String]))
        case "create-option" => throw new IllegalArgumentException("create-option")
      }
    }

    def on_input_change(p: Props, s: State): RSNC.OnInputChange = (value: String, action_meta: RSNC.ActionMeta) => {
//      dom.console.log(action_meta)
//      println(value, action_meta.action)
    }

    def on_menu_open(p: Props, s: State): RSNC.OnMenuOpen = () => {
      p.option_provider.get(NoFilter)
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
            case SomeValue(x) => Some(EnumeratedOption(x, x))
            case NoValue() => None
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

  implicit def FieldValue_reuse[A]: Reusability[FieldValue[A]]  = Reusability.by_==
  implicit val props_reuse = Reusability.by((_ : Props).value)
  implicit val choices_reuse = Reusability.by_==[Seq[EnumeratedOption]]
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

  def apply(field : RecordField, record_id: String, value : FieldValue[String],
            action_listener: Observer[EditorAction], option_provider: FieldOptionProvider) =
    component(Props(field, record_id, value, action_listener, option_provider))
}