package sysmo.demo1

import sysmo.reform.app.{Configuration, Panel}
import sysmo.reform.explorers.{FormExplorerF, InstanceTableF, ObjectTreeBrowserF}
import sysmo.reform.shared.examples.MicroController
import sysmo.reform.shared.util.containers.FLocal
import sysmo.reform.shared.runtime.{RFObject, RFRuntime}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import sysmo.reform.effects.implicits._
import sysmo.reform.react.layouts.{NamedContent, TabbedLayout}


object RuntimeExplorerPanel extends Panel {
  type F[+X] = FLocal[X]
  object FormExplorer extends FormExplorerF[F]

  case class Props(root_obj: F[RFObject[F]])
  case class State()

  final class Backend($: BScope) {
    def render(p: Props, s: State): VdomElement = {
      FormExplorer(p.root_obj)
    }
  }


  val component = ScalaComponent.builder[Props]("RuntimeExplorer")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(app_config: Configuration): Unmounted = {
    component(Props(MicroController.initializer1()))
  }
}