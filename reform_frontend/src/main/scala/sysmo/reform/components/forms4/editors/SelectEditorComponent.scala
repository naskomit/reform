package sysmo.reform.components.forms4.editors

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ScalaComponent}
import sysmo.reform.components.forms4.FormDataHandler
import sysmo.reform.components.select.ReactSelectFacades.{ReactSelectNativeComponent => RSNC}
import sysmo.reform.shared.util.LabeledValue
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sysmo.reform.shared.{form => F}

import scala.scalajs.js

object SelectEditorComponent extends AbstractEditor[String] {
  case class Props(editor: F.SelectEditor, data_handler: FormDataHandler) {
    def value: FieldValueType = data_handler.get_value(editor).asInstanceOf[FieldValueType]
  }

  case class State(choices: Seq[LabeledValue[_]], is_loading: Boolean)

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      <.div(^.className := "form-group", ^.key := p.editor.name, ^.id := p.editor.path.toString,
        <.label(p.editor.descr),
        RSNC.builder
          .value(p.value).options(s.choices)
          .map(b => if (p.editor.multiple) b.multiple())
          .on_change(on_change(p, s))
          .on_input_change(on_input_change(p, s))
          .on_menu_open(on_menu_open(p, s))
          .build
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
    def find_choice(v: Any, choices: Seq[LabeledValue[_]]): Option[LabeledValue[_]] =
      choices.find(c => c.value == v)

    def update_selection(p: Props, s: State)(selection: RSNC.OneOrMoreSelected): Unit = {
      selection match {
        case many: js.Array[RSNC.Choice] => {
          val original_choice: Seq[LabeledValue[_]] = many.map(x => find_choice(x.value, s.choices)).collect {
            case Some(x) => x
          }.toSeq
          p.data_handler.dispatch(SetFieldValue(p.editor.path, F.MultiValue(original_choice)))
        }
        case one => {
          find_choice(one.asInstanceOf[RSNC.Choice].value, s.choices) match {
            case Some(x) => p.data_handler.dispatch(SetFieldValue(p.editor.path, F.SomeValue(x)))
            case None =>
          }
        }
      }
    }

    def on_change(p: Props, s: State): RSNC.OnChange = (selection: RSNC.OneOrMoreSelected, action_meta: RSNC.ActionMeta) => {
//      org.scalajs.dom.console.log(selection)
//      org.scalajs.dom.console.log(action_meta.action)
      //logger.info(s"[on_change]: ${action_meta.action} / ${choice.value} : ${choice.label}")

      action_meta.action match {
        case "select-option" => update_selection(p, s)(selection)
        case "deselect-option" => throw new IllegalArgumentException("deselect-option")
        case "remove-value" => update_selection(p, s)(selection)
        case "pop-value" => throw new IllegalArgumentException("pop-value")
        case "clear" => p.data_handler.dispatch(SetFieldValue(p.editor.path, F.NoValue))
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
    ScalaComponent.builder[Props]("SelectEditor")
      .initialState(State(Seq(), is_loading = true))
      .renderBackend[Backend]
      .componentDidMount(f => f.backend.load_choices(f.props, f.state))
//            .configure(Reusability.shouldComponentUpdate)
      .build

  def apply(editor: F.SelectEditor, data_handler: FormDataHandler): Unmounted = {
    component(Props(editor, data_handler))
  }

}
