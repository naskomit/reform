package sysmo.demo1

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.app.{Configuration, Panel}
import sysmo.reform.effects.implicits._
import sysmo.reform.explorers.FormExplorerF
import sysmo.reform.shared.examples.{BioReactorController, MicroController}
import sysmo.reform.shared.runtime.{LocalRuntime, LocalRuntimeConstructor, RFObject}
import sysmo.reform.shared.containers.FLocal


object BioReactorControllerPanel extends Panel {
  type F[+X] = FLocal[X]
  object FormExplorer extends FormExplorerF[F]

  case class Props(root_obj: F[RFObject[F]])
  case class State()

  final class Backend($: BScope) {
    def render(p: Props, s: State): VdomElement = {
      FormExplorer(p.root_obj)
    }
  }


  val component = ScalaComponent.builder[Props]("BioReactorController")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(app_config: Configuration): Unmounted = {
    component(Props(BioReactorController.initializer1(LocalRuntimeConstructor).root))
  }
}