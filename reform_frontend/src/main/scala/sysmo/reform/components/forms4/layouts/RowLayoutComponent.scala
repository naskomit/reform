package sysmo.reform.components.forms4.layouts

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.components.forms4.{transitions => Tr}

object RowLayoutComponent extends ArrayGroupLayout {
  case class Props(title: String, child_elements: Seq[ArrayChildElement], options: FormRenderingOptions)
  case class State(expanded: Boolean)

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) : VdomNode = {
      val header_fn = (p.options.get(_.depth) + 1) match {
        case 1 => <.h1
        case 2 => <.h2
        case 3 => <.h3
        case 4 => <.h4
      }

      val chevron = <.i(
        ^.classSet1("fa", "fa-chevron-down" -> s.expanded, "fa-chevron-right" -> !s.expanded),
        ^.fontSize := "0.6em",
        ^.verticalAlign := "20%",
        ^.onClick --> toggle_expanded
      )

      val rows = p.child_elements.map(child => {
        <.div(^.className:= "row",
          <.div(^.className:= "col-md-12",
            child.child
          )
        )
      })

      <.div(^.className:="wrapper wrapper-white",
        header_fn(chevron, " ", p.title),
        Tr.collapsible(s.expanded, rows)
      )
    }

    def toggle_expanded: Callback = $.modState(s => s.copy(expanded = !s.expanded))

  }

  val component = ScalaComponent.builder[Props]("RowLayoutComponent")
    .initialState(State(true))
    .renderBackend[Backend]
    //    .render_PCS((p, c, s) => (new Backend).render(p, c, s))
    .build

  override def apply(title: String, children: Seq[ArrayChildElement], options: FormRenderingOptions): Unmounted = {
    component(Props(title, children, options))
  }

}


