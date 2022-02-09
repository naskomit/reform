package sysmo.coviddata.components.editors

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import monix.execution.{Ack, Cancelable}
import monix.execution.cancelables.SingleAssignCancelable
import monix.reactive.{Observable, Observer, OverflowStrategy}
import monix.execution.Scheduler.Implicits.global
import sysmo.coviddata.components.actions.ActionStreamGenerator

object StringEditor extends AbstractEditor {
  case class Props(id : String, manager_id : String, label : String, value : String,
                   focused: Boolean, action_listener: Observer[EditorAction])
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    println("Created StringEditor backend")
    val action_generator : ActionStreamGenerator[EditorAction] = ActionStreamGenerator[EditorAction]

    def render (p: Props, s: State): VdomElement = {
      <.div(^.className:= "form-group", ^.key:= p.id,
        <.label(p.label),
        <.input(^.`type`:= "text", ^.className:= "form-control", ^.autoFocus:= p.focused,
          ^.value := p.value, ^.onChange ==> on_value_change,
          ^.onFocus --> on_focus, ^.onBlur --> on_blur
      ))
    }

    def on_value_change(event : ReactEventFromInput) : Callback = Callback {
      action_generator.dispatch(ValueChanged(event.target.value))
    }

    def on_focus = Callback {
      action_generator.dispatch(GotFocus())
    }

    def on_blur = Callback {
      action_generator.dispatch(LostFocus())
    }
  }


//action_listener: Observer[EditorAction]
  val component =
    ScalaComponent.builder[Props]("StringEditor")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(f => Callback {
      println("StringEditor mounted")
      f.backend.action_generator.start(f.props.action_listener)
    })
    .build

  def apply(id : String, manager_id : String, label : String, value : String,
            action_listener: Observer[EditorAction], focused : Boolean = false) = {
    println("StringEditor creating")
    component(Props(id, manager_id, label, value, focused, action_listener))

  }

}
