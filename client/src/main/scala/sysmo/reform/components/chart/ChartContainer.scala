package sysmo.reform.components.chart

import japgolly.scalajs.react.vdom.html_<^.{<, _}
import org.scalajs.dom
import sysmo.reform.components.menu.ButtonToolbar
import sysmo.reform.components.{ReactAction, ReactComponent}
import sysmo.reform.managers.ChartController
import sysmo.reform.shared.chart.ChartSettings
import sysmo.reform.shared.{chart => Ch}
import sysmo.reform.util.TypeSingleton
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.reflect.ClassTag
import scala.scalajs.js



object ChartContainerDefs {
  sealed trait ActiveMode
  case object ActiveChart extends ActiveMode
  case object Loading extends ActiveMode
  case object ActiveSettings extends ActiveMode
  trait ChartAction extends ReactAction
  case object ActivateChart extends ChartAction
  case object ActivateSettings extends ChartAction
  case object OkSettings extends ChartAction
  case object CancelSettings extends ChartAction

  case class Props[U <: ChartSettings](chart_manager: ChartController[U], height: Int)
  case class State(chart_result: Option[Ch.ChartResult], mode: ActiveMode)

}


class ChartContainer[U <: ChartSettings : ClassTag] extends ReactComponent {
  import japgolly.scalajs.react._
  import ChartContainerDefs._

  //  trait ChartEffect extends ReactEffect

  type Props = ChartContainerDefs.Props[U]
  type State = ChartContainerDefs.State

  final class Backend($: BackendScope[Props, State]) {

    object Effects {
      def update_chart_result(props: Props): AsyncCallback[Unit] =
        AsyncCallback.fromFuture(
          props.chart_manager.chart_service.chart(props.chart_manager.request())
        ).flatMap(res =>
          $.modState(s => s.copy(chart_result = Some(res))).asAsyncCallback
        ).handleError(
          err => $.modState(s => s.copy(chart_result = None)).asAsyncCallback
        )

      def activate_mode(mode: ActiveMode): AsyncCallback[Unit] =
        $.modState(s => s.copy(mode = mode)).asAsyncCallback

      def ok_settings(props: Props): AsyncCallback[Unit] = Effects.activate_mode(Loading) >>
        AsyncCallback.pure {logger.info("Beginning fetch data")} >>
        Effects.update_chart_result(props) >>
        Callback {logger.info("Done fetch data")}.asAsyncCallback >>
        Effects.activate_mode(ActiveChart)
    }

//    case class Actions() {
//      val activate_settings = effects.activate_mode(ActiveSettings)
//    }

    def render(p: Props, s: State): VdomElement = {
//      val dsp = dispatch(p, s) _
      <.div(
        <.div(
          ^.style := js.Dictionary("height" -> p.height),
          s.mode match {
            case ActiveChart => <.div(Chart(s.chart_result))
            case ActiveSettings => <.div(
              ChartSettingsForm(p.chart_manager)
            )
            case Loading => <.div(
              ^.style := js.Dictionary("padding" -> "300px"),
              <.h1("Loading")
            )
          }
        ),
        <.div(
          ^.style := js.Dictionary("height" -> 50),
          s.mode match {
            case ActiveChart => ButtonToolbar.builder
                .button("Settings", Effects.activate_mode(ActiveSettings))
                .build

            case ActiveSettings => ButtonToolbar.builder
                .button("Ok", Effects.ok_settings(p))
                .button("Cancel", Effects.activate_mode(ActiveChart))
                .build

            case Loading => <.div()
          }
        )
      )
    }

//    // TODO Figure out how to properly handle the callbacks and refresh on new chart
//    override def handle_action(props: Props, state: State)(action: ReactAction): AsyncCallback[Unit] = {
//      val dsp = dispatch(props, state) _
//      val effects = Effects(props)
//      logger.info(action.toString)
//      action match {
//        case ActivateSettings => effects.activate_mode(ActiveSettings)
//        case OkSettings => effects.activate_mode(Loading) >>
//          AsyncCallback.pure {logger.info("Beginning fetch data")} >>
//          effects.update_chart >>
//          Callback {logger.info("Done fetch data")}.asAsyncCallback >>
//          effects.activate_mode(ActiveChart)
//        case CancelSettings => effects.activate_mode(ActiveChart)
//      }
//    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("ChartContainer")
      .initialState(State(None, ActiveChart))
      .renderBackend[Backend]
      // f.backend.perform_request(f.props)
      .componentDidMount(f => f.backend.Effects.ok_settings(f.props))
      .build
}

object ChartContainer extends TypeSingleton[ChartContainer, ChartSettings] {
  import ChartContainerDefs._

  override def create_instance[U <: ChartSettings](implicit tag: ClassTag[U]): ChartContainer[U] = new ChartContainer[U]
  def apply[U <: ChartSettings : ClassTag](chart_manager: ChartController[U], height: Int)(implicit tag: ClassTag[U]) = {
    get_instance(tag).component(Props[U](chart_manager, height))
  }
}
