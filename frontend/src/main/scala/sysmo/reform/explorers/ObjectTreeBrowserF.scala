package sysmo.reform.explorers

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.react.ReactComponent
import sysmo.reform.react.tree_nav.TreeNavigatorComponentF
import sysmo.reform.shared.runtime.{RFRuntime, RFObject}

class ObjectTreeBrowserF[F[+_]](implicit f2c: F2Callback[F]) extends ReactComponent {
  object TreeNavigatorComponent extends TreeNavigatorComponentF[RFRuntime.TreeTypes, F]

  case class Props(obj: RFObject[F], runtime: RFRuntime[F])
  case class State()


  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val tree = new RFObject.TreeView[F](p.obj)
      <.div(
        <.div(^.maxWidth:= 300.px,
          TreeNavigatorComponent(tree)
        ),
        <.div(^.maxWidth:= 300.px,
//          PropertyEditor(tree)
        )
      )
    }
  }

  val component =
    ScalaComponent.builder[Props]("RuntimeExplorer")
      .initialState(State())
      .renderBackend[Backend]
//      .componentDidMount(f => f.backend.start(f.props))
      .build

  //
  def apply(obj: RFObject[F], runtime: RFRuntime[F]): Unmounted = {
    component(Props(obj, runtime))
  }
}
