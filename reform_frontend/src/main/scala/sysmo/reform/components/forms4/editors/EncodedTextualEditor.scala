package sysmo.reform.components.forms4.editors

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ReactEventFromInput, ScalaComponent}
import sysmo.reform.components.forms4.FormDataHandler
import sysmo.reform.shared.data.{form4 => F}
import sysmo.reform.shared.util.LabeledValue

trait EncodedTextualEditor[VT] extends AbstractEditor[VT] {
  val display_name: String
  type EditorType <: F.FieldEditor
  case class Props(editor: EditorType, data_handler: FormDataHandler) {
    def value: FieldValueType = data_handler.get_value(editor).asInstanceOf[FieldValueType]
  }
  case class State(local_value: String, status: Status, focused: Boolean)

  def format(value: FieldValueType): String
  def parse(s: String): Option[VT]


  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val fg_classes = Seq("form-group") ++ (s.status match {
        case Valid => Seq()
        case Error(_, _) => Seq("has-error")
        case Warning(_, _) => Seq("has-warning")
      })
      <.div(^.className := fg_classes.mkString(" "), ^.key := p.editor.name, ^.id := p.editor.descr,
        <.label(p.editor.descr),
        <.input(^.`type` := "text", ^.className := "form-control", ^.autoFocus := false,
          ^.value := ((s.focused, s.status) match {
            case (false, Valid | Warning(_, _)) => format(p.value)
            case _ => s.local_value
          })
          ,
          ^.onChange ==> on_value_change(p),
          ^.onFocus --> on_focus, ^.onBlur --> on_blur(p, s)
        ),
        s.status match {
          case Valid => ""
          case Error(msg, value) => <.span(^.className := "help-block", msg)
          case Warning(msg, value) => <.span(^.className := "help-block text-danger", msg)
        }

      )
    }

    def on_value_change(p: Props)(event: ReactEventFromInput): Callback =
      $.modState(s => s.copy(local_value = event.target.value))

    def on_focus: Callback =
      $.modState(s => s.copy(focused = true))

    def on_blur(p: Props, s: State): Callback = {
      val value: Option[VT] = parse(s.local_value)
      value match {
        case Some(x) => $.modState(s => s.copy(
          local_value = format(F.SomeValue(LabeledValue(x))), status = Valid, focused = false
        )) >> Callback {
          p.data_handler.dispatch(SetFieldValue(p.editor.path, F.SomeValue(LabeledValue(x))))
        }
        case None => $.modState(s => s.copy(status = Error("Invalid value", s.local_value)))
      }
    }
  }


  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props](display_name)
      .initialStateFromProps(p => State(format(p.value), Valid, focused = false))
      .renderBackend[Backend]
      .build

}
