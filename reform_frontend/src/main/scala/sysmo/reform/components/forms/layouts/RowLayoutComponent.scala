package sysmo.reform.components.forms.layouts

import japgolly.scalajs.react.{CtorType, _}
import japgolly.scalajs.react.component.Scala.Component
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.forms.{transitions => Tr}
import sysmo.reform.components.menu.ButtonToolbar

object RowLayoutComponent extends ArrayGroupLayout {

  case class State(expanded: Boolean)

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) : VdomNode = {
      val rows = p.child_elements.zipWithIndex.map(item => {
        val odd = (item._2 % 2) == 1
        <.div(^.classSet1("wrapper", "wrapper-dark" -> !odd, "wrapper-white" -> odd),
          <.div(^.className:= "row",
            <.div(^.className:= "col-md-12",
              item._1.child
            )
          )
        )
      })

      <.div(^.className:="wrapper wrapper-neutral",
        p.menu match {
          case Some(x) => x.component
          case None => <.span()
        },
        rows.toTagMod
      )
    }

    def toggle_expanded: Callback = $.modState(s => s.copy(expanded = !s.expanded))

  }

  val component = ScalaComponent.builder[Props]("RowLayoutComponent")
    .initialState(State(true))
    .renderBackend[Backend]
    //    .render_PCS((p, c, s) => (new Backend).render(p, c, s))
    .build

//  override def apply(children: Seq[ArrayChildElement], menu: Option[ButtonToolbar.Props], options: FormRenderingOptions): Unmounted = {
//    component(Props(children, menu, options))
//  }

}


