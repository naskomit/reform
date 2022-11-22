package sysmo.reform.widgets.tooltip

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import sysmo.reform.widgets.ReactComponent


object TooltipViewer extends ReactComponent {

  case class Props()
  case class State(x: Double, y: Double, content: Option[facade.React.Node])
  type StateUpdater = (State => State) => CallbackTo[Unit]

  var state_updater: Option[StateUpdater] = None

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      s.content match {
        case Some(content) =>
          <.div(^.position := "fixed", ^.top := (s.y + 10).px, ^.left := (s.x + 10).px, ^.backgroundColor:= "white",
            <.div(^.zIndex := "1000", content)
          )
        case None => <.div()
      }
    }

    val update_state: StateUpdater = f => $.modState(s => f(s))
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("Tooltip")
      .initialState(State(0, 0, None))
      .renderBackend[Backend]
      .componentDidMount(f => Callback {state_updater = Some(f.backend.update_state)})
      .componentWillUnmount(f => Callback{state_updater = None})
      .build

  //
  def apply(): Unmounted = {
    component(Props())
  }

  def show(x: Double , y: Double, content: facade.React.Node): Unit =
    state_updater match {
      case Some(update) => update(s => s.copy(x = x, y = y, content = Some(content))).runNow()
      case None => logger.warn("No component to show the tooltip")
    }

 def hide(): Unit =
   state_updater match {
     case Some(update) => update(s => s.copy(content = None)).runNow()
     case None =>
   }
}
