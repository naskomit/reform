package sysmo.reform.components.forms4.editors

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ReactEventFromInput, ScalaComponent}
import sysmo.reform.components.forms4.FormDataHandler
import sysmo.reform.components.select.ReactSelectFacades.{ReactSelectNativeComponent => RSNC}
import sysmo.reform.shared.data.{form4 => F}
import sysmo.reform.shared.util.LabeledValue
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._

object SelectEditorComponent extends AbstractEditor[String] {
  case class Props(editor: F.SelectEditor, data_handler: FormDataHandler) {
    def value: FieldValueType = data_handler.get_value(editor).asInstanceOf[FieldValueType]
  }

  case class State(choices: Seq[LabeledValue[_]], is_loading: Boolean)

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val value = p.value match {
        case F.SomeValue(v) => Some(label_value(v, s))
        case _ => None
      }

      <.div(^.className := "form-group", ^.key := p.editor.name, ^.id := p.editor.path.toString,
        <.label(p.editor.descr),
        RSNC(
          value, s.choices,
          is_loading = Some(s.is_loading),
          on_change = Some(on_change(p, s)),
          on_input_change = Some(on_input_change(p, s)),
          on_menu_open = Some(on_menu_open(p, s))
        )
      )
    }

    def label_value(x: LabeledValue[_], s: State): LabeledValue[_] = x match {
      case LabeledValue(value, Some(label)) => x
      case LabeledValue(value, None) if s.choices.nonEmpty => s.choices.find(_.value == value).getOrElse(x)
      case _ => x
    }

    def load_choices(p: Props, s: State): AsyncCallback[Unit] = {
      $.modState(s => s.copy(is_loading = true)).async >>
        AsyncCallback.fromFuture(p.data_handler.get_choices(p.editor)) >>=
        {choices => $.modState(s => s.copy(choices = choices, is_loading = false)).async}
    }
    def on_change(p: Props, s: State): RSNC.OnChange = (choice: RSNC.Choice, action_meta: RSNC.ActionMeta) => {
      logger.info(s"[on_change]: ${action_meta.action} / ${choice.value} : ${choice.label}")
      action_meta.action match {
        case "select-option" => {
          val original_choice = s.choices.find(c => c.value == choice.value).get
          p.data_handler.dispatch(SetFieldValue(p.editor.path, F.SomeValue(original_choice)))
        }
        case "deselect-option" => throw new IllegalArgumentException("deselect-option")
        case "remove-value" => throw new IllegalArgumentException("remove-value")
        case "pop-value" => throw new IllegalArgumentException("pop-value")
        case "clear" => throw new IllegalArgumentException("pop-value")
        case "create-option" => throw new IllegalArgumentException("create-option")
      }
    }

    def on_input_change(p: Props, s: State): RSNC.OnInputChange = (value: String, action_meta: RSNC.ActionMeta) => {
//      logger.info(value)
//      logger.info(action_meta.action)
    }

    def on_menu_open(p: Props, s: State): RSNC.OnMenuOpen = () => load_choices(p, s).runNow()
  }


  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props]("StringEditor")
      .initialState(State(Seq(), is_loading = true))
      .renderBackend[Backend]
      .componentDidMount(f => f.backend.load_choices(f.props, f.state))
//            .configure(Reusability.shouldComponentUpdate)
      .build

  def apply(editor: F.SelectEditor, data_handler: FormDataHandler): Unmounted = {
    component(Props(editor, data_handler))
  }

}
