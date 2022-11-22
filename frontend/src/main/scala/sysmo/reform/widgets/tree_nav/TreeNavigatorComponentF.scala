package sysmo.reform.widgets.tree_nav

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import sysmo.reform.widgets.ReactComponent
import sysmo.reform.shared.sources.{tree => T}
import sysmo.reform.css.{ReFormCSS => CSS}
import sysmo.reform.effects.implicits.F2Callback

class TreeNavigatorComponentF[F[+_]](implicit f2c: F2Callback[F])
  extends ReactComponent {
  val TreeNavigatorItem = new TreeNavigatorItemF[F]

  case class Props(data: T.TreeSource[F])
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(CSS.tree_nav.nav,
        TreeNavigatorItem(p.data.root, 0, TreeNavigatorItemF.Settings())
      )
    }
  }



  val component =
    ScalaComponent.builder[Props]("TreeNavigator")
      .initialState(State())
      .renderBackend[Backend]
//      .componentDidMount(f => Callback {
//        f.props.data.renderer = Some(f.backend.R)
//      })
      .build

  def apply(data: T.TreeSource[F]): Unmounted =
    component(Props(data))

}

