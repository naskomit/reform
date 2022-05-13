package sysmo.reform.components.forms3.editors

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ReactEventFromInput, ScalaComponent}
import sysmo.reform.components.forms3.FormDataHandler
import sysmo.reform.shared.data.form3.{BooleanEditor, FormData => FD}
import sysmo.reform.shared.util.LabeledValue

object BooleanEditorComponent extends AbstractEditor[Boolean] {
  case class Props(editor: BooleanEditor, value: FieldValueType, data_handler: FormDataHandler)
  case class State()

  def format(v: FieldValueType): Boolean = {
    v match {
      case FD.SomeValue(LabeledValue(true, _)) => true
      case _ => false
    }

  }

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      <.div(^.className := "form-group", ^.key := p.editor.name,
        ^.id := p.editor.path.toString, ^.onClick ==> on_click(p),
        <.label(p.editor.make_label),
        <.br(),
        <.div(^.className := "checkbox checkbox-inline",
          <.input(^.`type` := "checkbox", ^.autoFocus := false,
          ^.checked := format(p.value), ^.onChange ==> on_value_change(p)),
          <.label("")
        )
      )
    }

    def on_value_change(p: Props)(event: ReactEventFromInput): Callback = Callback {
      println(s"Checkbox value ${p.editor.path} changed ")
      //      action_generator.dispatch(SetValue(SomeValue(LabeledValue(event.target.value))))
    }

    def on_click(p: Props)(event: ReactEventFromInput): Callback = Callback {
      p.data_handler.dispatch(
        SetFieldValue(p.editor.path, FD.SomeValue(LabeledValue(!format(p.value))))
      )
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

  def apply(editor: BooleanEditor, value: FieldValueType, data_handler: FormDataHandler): Unmounted = {
    component(Props(editor, value, data_handler))

  }

}
