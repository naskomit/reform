package sysmo.reform.components.forms3.editors

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ReactEventFromInput, ScalaComponent}
import sysmo.reform.components.forms3.FormDataHandler
import sysmo.reform.components.select.ReactSelectFacades.{ReactSelectNativeComponent => RSNC}
import sysmo.reform.shared.data.form3.{SelectEditor, FormData => FD}
import sysmo.reform.shared.util.LabeledValue

object SelectEditorComponent extends AbstractEditor[String] {
  case class Props(editor: SelectEditor, value: FD.CharValue, data_handler: FormDataHandler)

  case class State()

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val value = p.value match {
        case FD.SomeValue(v) => Some(v)
        case _ => None
      }
      val choices: Seq[LabeledValue[_]] = Seq()
      <.div(^.className := "form-group", ^.key := p.editor.name, ^.id := p.editor.path.toString,
        <.label(p.editor.make_label),
        RSNC(value, choices)
      )
    }

    def on_value_change(event: ReactEventFromInput): Callback = Callback {
      //      action_generator.dispatch(SetValue(SomeValue(LabeledValue(event.target.value))))
    }

    def on_focus = Callback {
      //      action_generator.dispatch(GotFocus())
    }

    def on_blur = Callback {
      //      action_generator.dispatch(LostFocus())
    }
  }


  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props]("StringEditor")
      .initialState(State())
      .renderBackend[Backend]
      //      .componentDidMount(f => Callback {
      //        f.backend.action_generator.start(f.props.action_listener)
      //      })
      //      .configure(Reusability.shouldComponentUpdate)
      .build

  def apply(editor: SelectEditor, value: FD.CharValue, data_handler: FormDataHandler): Unmounted = {
    component(Props(editor, value, data_handler))
  }

}
