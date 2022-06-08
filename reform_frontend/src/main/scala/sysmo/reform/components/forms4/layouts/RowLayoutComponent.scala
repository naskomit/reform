package sysmo.reform.components.forms4.layouts

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import sysmo.reform.components.forms4.options.FormRenderingOptions

object RowLayoutComponent extends ArrayGroupLayout {
  case class Props(title: String, child_elements: Seq[ArrayChildElement], options: FormRenderingOptions)
  type State = Unit
  type Backend = Unit

  class Builder {
    def render(p: Props, children: PropsChildren) : VdomNode = {
      val header_fn = (p.options.get(_.depth) + 1) match {
        case 1 => <.h1
        case 2 => <.h2
        case 3 => <.h3
        case 4 => <.h4
      }

      val rows = children.toList.map(child => {
        <.div(^.className:= "row",
          <.div(^.className:= "col-md-12",
            child
          )
        )
      })

      <.div(^.className:="wrapper wrapper-white",
        header_fn(p.title),
        rows.toTagMod
      )
    }
  }

  val component = ScalaComponent.builder[Props]("RowLayoutComponent")
    .render_PC((p, c) => (new Builder).render(p, c))
    .build

  override def apply(title: String, children: Seq[ArrayChildElement], options: FormRenderingOptions): Unmounted = {
    component(Props(title, children, options))(children.map(x => x.child): _*)
  }

}


