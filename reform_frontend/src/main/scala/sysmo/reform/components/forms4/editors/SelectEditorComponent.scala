package sysmo.reform.components.forms4.editors

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ReactEventFromInput, ScalaComponent}
import sysmo.reform.components.forms4.FormDataHandler
import sysmo.reform.components.select.ReactSelectFacades.{ReactSelectNativeComponent => RSNC}
import sysmo.reform.shared.data.{form4 => F}
import sysmo.reform.shared.util.LabeledValue
import scala.concurrent.ExecutionContext.Implicits.global

object SelectEditorComponent extends AbstractEditor[String] {
  case class Props(editor: F.SelectEditor, data_handler: FormDataHandler) {
    def value: FieldValueType = data_handler.get_value(editor).asInstanceOf[FieldValueType]
  }

  case class State(choices: Seq[LabeledValue[_]], is_loading: Boolean)

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val value = p.value match {
        case F.SomeValue(v) => Some(v)
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

    def on_change(p: Props, s: State): RSNC.OnChange = (choice: RSNC.Choice, action_meta: RSNC.ActionMeta) => {
      logger.info(action_meta.action)
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
      logger.info(value)
      logger.info(action_meta.action)
    }

    def on_menu_open(p: Props, s: State): RSNC.OnMenuOpen = () => {
      // load choices
      p.data_handler.get_choices(p.editor)
        .map(choices => $.modState(s => s.copy(choices = choices)).runNow())
    }
  }


  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props]("StringEditor")
      .initialState(State(Seq(), is_loading = false))
      .renderBackend[Backend]
      //      .componentDidMount(f => Callback {
      //        f.backend.action_generator.start(f.props.action_listener)
      //      })
      //      .configure(Reusability.shouldComponentUpdate)
      .build

  def apply(editor: F.SelectEditor, data_handler: FormDataHandler): Unmounted = {
    component(Props(editor, data_handler))
  }

}
