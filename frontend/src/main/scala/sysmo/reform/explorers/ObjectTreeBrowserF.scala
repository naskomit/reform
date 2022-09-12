package sysmo.reform.explorers

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.react.ReactComponent
import sysmo.reform.react.tree_nav.TreeNavigatorComponentF
import sysmo.reform.react.property.PropertyGroupEditorF
import sysmo.reform.shared.runtime.{RFObject, RFRuntime}

import scala.scalajs.js

class ObjectTreeBrowserF[F[+_]](implicit f2c: F2Callback[F]) extends ReactComponent {
  object TreeNavigatorComponent extends TreeNavigatorComponentF[RFRuntime.TreeTypes, F]
  object PropertyGroupEditor extends PropertyGroupEditorF[RFRuntime.PropertyTypes, F]

  case class Props(obj: RFObject[F], runtime: RFRuntime[F])
  case class State()


  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val tree_view = new RFObject.TreeView[F](p.obj)
      val prop_view = RFObject.NamedPropertyView(p.obj)
      <.div(
        ^.display:= "flex", ^.columnGap:= 100.px,
        <.div(^.maxWidth:= 300.px, ^.flexGrow:= "1",
          TreeNavigatorComponent(tree_view)
        ),
        <.div( ^.flexGrow:= "3",
          <.h2("Properties"),
          PropertyGroupEditor(prop_view)
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
