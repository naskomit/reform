package sysmo.reform.components.forms4.layouts

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.components.forms4.{transitions => Tr}

import scala.collection.mutable

object ColumnsLayoutComponent extends FormGroupLayout {
  case class Props(title: Option[String], child_elements: Seq[GroupChildElement], options: FormRenderingOptions)
  case class State(expanded: Boolean)

  class Builder {
    private val row_nodes: mutable.ArrayBuffer[VdomNode] = mutable.ArrayBuffer()
    private var row_filled_width = 0.0
    private val grid: mutable.ArrayBuffer[VdomElement] = mutable.ArrayBuffer()
    private var row_index = 0
    private var elem_index = 0
    private val base_col_width = 0.33

    def compute_width(elem: GroupChildElement): Double  = {
      val size = size_hint(elem.child) match {
        case ExtraShort => base_col_width * 0.25
        case Short => base_col_width * 0.5
        case Medium => base_col_width
        case Long => base_col_width * 1.5
        case ExtraLong => base_col_width * 2
        case FullWidth => 0.95

      }
      Math.max(size, 0.1)
    }

    def build_row(): Unit = {
      val key = row_index.toString
      grid += <.div(^.className:= f"row $key", ^.key:= key, row_nodes.toTagMod)
      row_index += 1
      elem_index = 0
      row_filled_width = 0.0
      row_nodes.clear()
    }

    def build_content(p: Props): Seq[VdomElement] = {
      for (elem <- p.child_elements) {
        val elem_width = compute_width(elem)
        if (row_filled_width + elem_width > 1.0) {
          build_row()
        }

        val element_node = <.div(
          ^.className:= f"col-md-${Math.round(elem_width * 12)} $elem_index",
          ^.key:= elem_index, render_child(elem)
        )
        row_nodes += element_node
        row_filled_width += elem_width
        elem_index += 1
      }

      if (row_nodes.nonEmpty) {
        build_row()
      }
      grid.toSeq
    }
  }

  class Backend($: BackendScope[Props, State]) {


    def render(p: Props, s: State) : VdomNode = {

      val content = (new Builder).build_content(p)

      <.div(^.className:= "wrapper wrapper-white",
        CollapsibleSection(
          p.title, p.options.get(_.depth) + 1, content
        )
      )
    }



  }

  val component = ScalaComponent.builder[Props]("ColumnsLayoutComponent")
    .initialState(State(true))
    .renderBackend[Backend]
//    .render_PCS((p, c, s) => (new Backend).render(p, c, s))
    .build

  override def apply(title: Option[String], children: Seq[GroupChildElement], options: FormRenderingOptions): Unmounted =
    component(Props(title, children, options))

}


