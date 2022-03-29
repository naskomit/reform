package sysmo.reform.components.editors

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, Observer}
import monix.execution.Scheduler.Implicits.global
import sysmo.reform.components.actions.ActionStreamGenerator
import sysmo.reform.components.select.ReactSelectFacades
import sysmo.reform.shared.data.EnumeratedOption


import scala.concurrent.Future

object SelectEditor extends AbstractEditor {
  case class Props(id : String, manager_id : String, label : String, value : String,
                   choices: Seq[EnumeratedOption],
                   action_listener: Observer[EditorAction])
  case class State()

  final class Backend($: BackendScope[Props, State]) {
//    println("Created SelectEditor backend")
    val action_generator : ActionStreamGenerator[EditorAction] =
      ActionStreamGenerator[EditorAction]

    def render(p: Props, s: State) : VdomElement = {
      <.div(^.className:= "form-group", ^.key:= p.id,
        <.label(p.label),
        ReactSelectFacades.ReactSelectNativeComponent(
          Some(p.choices(0)),
          p.choices
        )
      )
    }
  }

  implicit val props_reuse = Reusability.by((_ : Props).value)
  implicit val choices_reuse = Reusability.by_==[Seq[EnumeratedOption]]
  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props]("SelectEditor")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(f => Callback {
      f.backend.action_generator.start(f.props.action_listener)
    })
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(id : String, manager_id : String, label : String, value : String,
            choices: Seq[EnumeratedOption],
            action_listener: Observer[EditorAction]) =
    component(Props(id, manager_id, label, value, choices, action_listener))
}
