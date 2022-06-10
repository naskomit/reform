package sysmo.reform.components.forms4.layouts

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms4.{transitions => Tr}

object CollapsibleSection extends ReactComponent {

  case class Props(title: Option[String], depth: Int, content: Seq[VdomNode])
  case class State(expanded: Boolean)

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val title_node = p.title.map { title =>
        val header_fn = (p.depth + 1) match {
          case 1 => <.h1
          case 2 => <.h2
          case 3 => <.h3
          case 4 => <.h4
          case 5 => <.h5
          case 6 => <.h6
        }

        val chevron = <.i(
          ^.classSet1("fa",
            "fa-chevron-down" -> s.expanded,
            "fa-chevron-right" -> !s.expanded),
          ^.fontSize := "0.6em",
          ^.verticalAlign := "20%",
          ^.onClick --> toggle_expanded
        )
        header_fn(chevron, " ", title)
      }
      <.div(
        title_node,
        Tr.collapsible(s.expanded, p.content)
      )

    }

    def toggle_expanded: Callback = $.modState(s => s.copy(expanded = !s.expanded))
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("ReactComponentTemplate")
    .initialState(State(true))
    .renderBackend[Backend]
    .build

  //
  def apply(title: Option[String], depth: Int, content: Seq[VdomNode]): Unmounted = {
    component(Props(title, depth, content))
  }
}
