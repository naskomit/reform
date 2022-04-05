package sysmo.reform.components.editors

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import monix.reactive.{Observable, Observer, OverflowStrategy}
import sysmo.reform.components.actions.ActionStreamGenerator
import sysmo.reform.shared.data.form.{FieldValue, NoValue, SomeValue}
import sysmo.reform.shared.util.LabeledValue

object StringEditor extends AbstractEditor {
  case class Props(id : String, manager_id : String, label : String, value : FieldValue,
                   focused: Boolean, action_listener: Observer[EditorAction])
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    println("Created StringEditor backend")
    val action_generator : ActionStreamGenerator[EditorAction] = ActionStreamGenerator[EditorAction]

    def render (p: Props, s: State): VdomElement = {
      val editor_value = p.value match {
        case SomeValue(x) => x.toString
        case NoValue => "No value!!!"
        case _  => "Error!!!"
      }
      <.div(^.className:= "form-group", ^.key:= p.id,
        <.label(p.label),
        <.input(^.`type`:= "text", ^.className:= "form-control", ^.autoFocus:= p.focused,
          ^.value:= editor_value,
          ^.onChange ==> on_value_change,
          ^.onFocus --> on_focus, ^.onBlur --> on_blur
      ))
    }

    def on_value_change(event : ReactEventFromInput) : Callback = Callback {
      action_generator.dispatch(SetValue(SomeValue(LabeledValue(event.target.value))))
    }

    def on_focus = Callback {
      action_generator.dispatch(GotFocus())
    }

    def on_blur = Callback {
      action_generator.dispatch(LostFocus())
    }
  }


  // TODO
  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  implicit val props_reuse = Reusability.by((_ : Props).value)
  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props]("StringEditor")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(f => Callback {
      f.backend.action_generator.start(f.props.action_listener)
    })
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(id : String, manager_id : String, label : String, value : FieldValue,
            action_listener: Observer[EditorAction], focused : Boolean = false) = {
    println("StringEditor creating")
    component(Props(id, manager_id, label, value, focused, action_listener))

  }

}
