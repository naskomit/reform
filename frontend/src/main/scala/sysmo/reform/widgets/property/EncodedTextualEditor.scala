package sysmo.reform.widgets.property

import japgolly.scalajs.react.{ReactEventFromInput, ScalaComponent}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.runtime.SetFieldValue
import sysmo.reform.shared.containers.FLocal

trait EncodedTextualEditor[F[+_]] extends PropertyEditor[F] {
  case class State(local_value: String, status: PropertyEditor.Status, focused: Boolean)

  def format(value: Value): String
  def parse(s: String): FLocal[Value]
  val display_name: String

  final class Backend($: BScope) {

    def render(p: Props, s: State): VdomElement = {
      val status_cls =  s.status match {
        case PropertyEditor.Valid => "is-valid"
        case PropertyEditor.Error(_, _) => "has-error"
        case PropertyEditor.Warning(_, _) => "has-warning"
      }
      <.div(^.className:= status_cls,
        <.input(^.`type` := "text", ^.className := "form-control",
          ^.autoFocus := false,
          ^.value := ((s.focused, s.status) match {
            case (false, PropertyEditor.Valid | PropertyEditor.Warning(_, _)) => format(p.field.value)
            case _ => s.local_value
          })
          ,
          ^.onChange ==> on_value_change(p),
          ^.onFocus --> on_focus, ^.onBlur --> on_blur(p, s)
        ),
        s.status match {
          case PropertyEditor.Valid => ""
          case PropertyEditor.Error(msg, value) => <.span(^.className := "help-block", msg)
          case PropertyEditor.Warning(msg, value) => <.span(^.className := "help-block text-danger", msg)
        }

      )
    }

    def on_value_change(p: Props)(event: ReactEventFromInput): Callback =
      $.modState(s => s.copy(local_value = event.target.value))

    def on_focus: Callback =
      $.modState(s => s.copy(focused = true))

    def on_blur(p: Props, s: State): AsyncCallback[Unit] = {
      val value: FLocal[Value] = parse(s.local_value)
      value.to_either match {
        case Right(x) if (x != p.field.value) => f2c.async(p.dispatcher.dispatch(
          SetFieldValue(p.id, p.field.copy(value = x))
        )) >> $.modStateAsync(s => s.copy(
          local_value = format(x),
          status = PropertyEditor.Valid, focused = false
        ))

        case Right(x) => $.modStateAsync(s => s.copy(
          status = PropertyEditor.Valid, focused = false
        ))

        case Left(e) => $.modStateAsync(s => s.copy(
          status = PropertyEditor.Error("Invalid value", s.local_value)
        ))
      }
    }
  }

  val component =
    ScalaComponent.builder[Props](display_name)
      .initialStateFromProps(p => State(format(p.field.value), PropertyEditor.Valid, focused = false))
      .renderBackend[Backend]
      .build
}
