package sysmo.reform.components.forms.layouts

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.layouts.{NamedContent, TabbedLayout}

object TabbedLayoutComponent extends FormGroupLayout {
  case class Props(child_elements: Seq[GroupChildNode], options: FormRenderingOptions)
  case class State(expanded: Boolean)



  class Backend($: BackendScope[Props, State]) {


    def render(p: Props, s: State) : VdomNode = {
        TabbedLayout(
          p.child_elements.map(x => NamedContent(x.title.get, x.node(x.options)))
        )
    }



  }

  val component = ScalaComponent.builder[Props]("TabbedLayoutComponent")
    .initialState(State(true))
    .renderBackend[Backend]
    .build

  override def apply(children: Seq[GroupChildNode], options: FormRenderingOptions): Unmounted =
    component(Props(children, options))

}


