package sysmo.reform.components.forms3

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import sysmo.reform.components.{Processing, ReactComponent}
import sysmo.reform.components.forms3.editors.{EditorAction, SetFieldValue}
import sysmo.reform.shared.data.{form3 => F}
import F.{FormData => FD}
import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount

object FormComponent extends ReactComponent {
  case class Props(form: F.Form, initial_data: FD.ValueMap)
  case class State(data: Option[FD.ValueMap], dispatcher: Option[EditorAction.Dispatcher])

  final class Backend(val $: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      (s.data, s.dispatcher) match {
        case (Some(data), Some(dispatcher)) => FormEditorComponent(p.form, data, dispatcher)
        case _ => Processing()
      }

    }
  }

  class Dispatcher(f: Backend) extends EditorAction.Dispatcher {
    override def handle_action(action: EditorAction): Unit = {
      action match {
        case SetFieldValue(path, value) => f.$.modState(s => s.copy(data = s.data.map(_.update(path, value)))).runNow()
      }
    }
  }


  val component = ScalaComponent.builder[Props]("FormComponent")
    .initialState(State(None, None))
    .renderBackend[Backend]
    .componentDidMount(f =>
      f.modState(s => s.copy(
        dispatcher = Some(new Dispatcher(f.backend)),
        data = Some(f.props.initial_data)
      ))
    )
    .build


  def apply(form: F.Form, initial_data: FD.ValueMap): Unmounted = {
    component(Props(form, initial_data))
  }

}
