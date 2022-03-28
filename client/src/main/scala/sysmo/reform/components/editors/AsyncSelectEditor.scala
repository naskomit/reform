package sysmo.reform.components.editors

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import monix.reactive.Observer
import org.scalajs.dom
import sysmo.reform.components.ReactAction
import sysmo.reform.components.actions.ActionStreamGenerator
import sysmo.reform.components.select.ReactSelectFacades
import sysmo.reform.shared.data.{EnumeratedOption, OptionFilter, OptionProvider, RecordField}


object AsyncSelectEditorFSM {
  sealed trait EditorMode
  case object Unfocused extends EditorMode
  case object LoadingChoices extends EditorMode
  case object Focused extends EditorMode

  sealed trait Selection
  case object SelectedNone extends Selection
  case object SelectedAll extends Selection
  case object SelectedSome extends Selection

}

object AsyncSelectEditor extends AbstractEditor {
  import sysmo.reform.components.editors.{AsyncSelectEditorFSM => fsm}

  case class Props(field : RecordField, record_id: String, values : Seq[String],
                   action_listener: Observer[EditorAction])
  case class State(mode: fsm.EditorMode, selection: fsm.Selection, choices: Seq[EnumeratedOption])

  final class Backend($: BackendScope[Props, State]) extends IBackend {
//    println("Created SelectEditor backend")
    val action_generator : ActionStreamGenerator[EditorAction] =
      ActionStreamGenerator[EditorAction]

    case class Effects(props: Props) {
//      def update_choices: AsyncCallback[Unit] = {
//      }
    }

    // is_disabled: Option[Boolean] = None,
    // is_loading: Option[Boolean] = None,
    // is_clearable: Option[Boolean] = None,
    // is_searchable: Option[Boolean] = None
    val on_input_changed = (x: String, y: Any) => {
      println("On Input Changed")
      dom.console.log(x)
      dom.console.log(y)
    }
    val on_menu_open = () => {
      println("On menu open")
    }
    def render(p: Props, s: State) : VdomElement = {
      <.div(^.className:= "form-group", ^.key:= p.field.name,
        <.label(p.field.label),
        s.mode match {
          case fsm.Unfocused => ReactSelectFacades.ReactSelectNativeComponent(
            p.values.headOption.map(x => EnumeratedOption(x, x)),
            s.choices,
            on_input_changed = Some(on_input_changed),
            on_menu_open = Some(on_menu_open)
          )
        }
      )
    }


    override def handle_action(props: Props, state: State)(action: ReactAction): AsyncSelectEditor.AsyncCallback[Unit] = ???
  }
// TODO
//  implicit val props_reuse = Reusability.byIterator((_ : Props).values)
//  implicit val choices_reuse = Reusability.by_==[Seq[EnumeratedOption]]
//  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props]("SelectEditor")
    .initialState(State(fsm.Unfocused, fsm.SelectedNone, Seq()))
    .renderBackend[Backend]
    .componentDidMount(f => {
      println("SelectEditor mounted")
//      f.backend.subscribe_to_choices(f.props) >>
        Callback {
          f.backend.action_generator.start(f.props.action_listener)
      }
    })
//    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(field : RecordField, record_id: String, values : Seq[String],
            action_listener: Observer[EditorAction]) =
    component(Props(field, record_id, values, action_listener))
}
