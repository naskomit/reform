package sysmo.reform.components.forms.layouts

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.forms.{transitions => Tr}

object RowLayoutComponent extends ArrayGroupLayout {
  case class Props(child_elements: Seq[ArrayChildElement], options: FormRenderingOptions)
  case class State(expanded: Boolean)

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) : VdomNode = {
      val rows = p.child_elements.map(child => {
        <.div(^.className:= "row",
          <.div(^.className:= "col-md-12",
            child.child
          )
        )
      })

      <.div(^.className:="wrapper wrapper-white",
        CollapsibleSection(None, p.options.get(_.depth) + 1, rows)
      )
    }

    def toggle_expanded: Callback = $.modState(s => s.copy(expanded = !s.expanded))

  }

  val component = ScalaComponent.builder[Props]("RowLayoutComponent")
    .initialState(State(true))
    .renderBackend[Backend]
    //    .render_PCS((p, c, s) => (new Backend).render(p, c, s))
    .build

  override def apply(children: Seq[ArrayChildElement], options: FormRenderingOptions): Unmounted = {
    component(Props(children, options))
  }

}


