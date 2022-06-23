package sysmo.reform.components.forms.editors

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ReactEventFromInput, ScalaComponent}
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.util.LabeledValue

trait EncodedTextualEditor[FBT <: FB.AtomicField] extends AbstractEditor[FBT] {
  val display_name: String
//  case class Props(field_obj: FR.AtomicValue[FBT], options: FormRenderingOptions) {
//    def value: FieldValueType = field_obj.value.asInstanceOf[FieldValueType]
//  }
  case class State(local_value: String, status: Status, focused: Boolean)

  def format(value: FieldValueType): String
  def parse(s: String): Option[VT]


  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val status_cls =  s.status match {
        case Valid => "is-valid"
        case Error(_, _) => "has-error"
        case Warning(_, _) => "has-warning"
      }
      <.div(^.className:= status_cls,
        <.input(^.`type` := "text", ^.className := "form-control",
          ^.autoFocus := false,
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
          local_value = format(FR.SomeValue(LabeledValue(x))), status = Valid, focused = false
        )) >> Callback {
          p.obj.runtime.dispatch(
            FR.SetFieldValue(p.obj.id, FR.SomeValue(LabeledValue(x)))
          )
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
