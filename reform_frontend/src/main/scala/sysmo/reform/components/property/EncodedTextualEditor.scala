package sysmo.reform.components.property

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ReactEventFromInput, ScalaComponent}
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.property.Property
import sysmo.reform.shared.field.Value

trait EncodedTextualEditor extends ReactComponent {
  val display_name: String
  type Res[X] = Either[Throwable, X]
  def Ok[X](x: X): Res[X] = Right(x)
  trait Props {
    type K
    type V
    def property: Property[K, V]
    def format(value: Value[V]): String
    def parse(s: String): Res[Value[V]]

  }

  trait Status
  case object Valid extends Status
  case class Error(msg: String, value: String) extends Status
  case class Warning(msg: String, value: String) extends Status

  case class State(local_value: String, status: Status, focused: Boolean)



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
            case (false, Valid | Warning(_, _)) => p.format(p.property.get_value)
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
      p.parse(s.local_value) match {
        case Right(x) => $.modState(s => s.copy(
          local_value = p.format(x), status = Valid, focused = false
        )) >> Callback {
          p.property.update_value(x)
        }
        case Left(e) => $.modState(s => s.copy(status = Error(s"Invalid value. ${e.getMessage}", s.local_value)))
      }
    }
  }


  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props](display_name)
      .initialStateFromProps(p => State(p.format(p.property.get_value), Valid, focused = false))
      .renderBackend[Backend]
      .build

}
