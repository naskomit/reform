package sysmo.demo1

import sysmo.reform.app.{Configuration, Panel}
import sysmo.reform.explorers.RuntimeExplorerF
import sysmo.reform.shared.examples.MicroController
import sysmo.reform.shared.runtime.{FLocal, ObjectRuntime, RuntimeObject}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import sysmo.reform.effects.implicits._


object RuntimeExplorerPanel extends Panel {


  type F[+X] = FLocal[X]

  object RuntimeExplorer extends RuntimeExplorerF[F]

  case class Props(initial_obj: AsyncCallback[RuntimeObject[F]])

  case class State(current_object: Option[RuntimeObject[F]], runtime: Option[ObjectRuntime[F]])

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.cls := "page-title",
          <.h1("Runtime Explorer")
        ),
        <.div(^.cls := "wrapper wrapper-white",
          s.runtime match {
            case None => <.div("Loading")
            case Some(rt) => RuntimeExplorer(rt)
          }
        )
      )
    }

    def start(p: Props): AsyncCallback[Unit] = for {
      obj <- p.initial_obj
      res <- $.modStateAsync(s =>
        s.copy(current_object = Some(obj), runtime = Some(obj.get_runtime))
      )
    } yield res
  }

  val component = ScalaComponent.builder[Props]("RuntimeExplorer")
    .initialState(State(None, None))
    .renderBackend[Backend]
    .componentDidMount(f => f.backend.start(f.props))
    .build

  def apply(app_config: Configuration): Unmounted = {
    val controller = MicroController.initializer1()
    val f2c: F2Callback[F] = implicitly[F2Callback[F]]
    component(Props(f2c.async(controller)))
  }
}