package sysmo.reform.components.forms4.layouts

import japgolly.scalajs.react.{CtorType, _}
import japgolly.scalajs.react.component.Scala.Component
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, VdomNode}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.forms4.options.FormRenderingOptions

import scala.collection.mutable

object ColumnsLayoutComponent extends FormGroupLayout {
  case class Props(title: String, child_elements: Seq[GroupChildElement], options: FormRenderingOptions)
  type State = Unit
  type Backend = Unit

  class Builder {
    private val row_nodes: mutable.ArrayBuffer[VdomNode] = mutable.ArrayBuffer()
    private var row_filled_width = 0.0
    private val grid: mutable.ArrayBuffer[VdomElement] = mutable.ArrayBuffer()
    private var row_index = 0
    private var elem_index = 0
    private val base_col_width = 0.33

    def build_row: Unit = {
      val key = row_index.toString
      grid += <.div(^.className:= f"row $key", ^.key:= key, row_nodes.toTagMod)
      row_index += 1
      elem_index = 0
      row_filled_width = 0.0
      row_nodes.clear()
    }

    def compute_width(elem: GroupChildElement): Double  = {
      val size = elem.size match {
        case ExtraShort => base_col_width * 0.25
        case Short => base_col_width * 0.5
        case Medium => base_col_width
        case Long => base_col_width * 1.5
        case ExtraLong => base_col_width * 2
        case FullWidth => 0.95

      }
      Math.max(size, 0.1)
    }

    def render(p: Props, children: PropsChildren) : VdomNode = {
      logger.info(s"ColumnsLayout # children: ${children.count}" )
      for ((child, elem) <- children.toList.zip(p.child_elements)) {
        val elem_width = compute_width(elem)
        if (row_filled_width + elem_width > 1.0) {
          build_row
        }
        val element_node = <.div(^.className:= f"col-md-${Math.round(elem_width * 12)} $elem_index", ^.key:= elem_index, child)
        row_nodes += element_node
        row_filled_width += elem_width
        elem_index += 1
      }

      if (row_nodes.nonEmpty) {
        build_row
      }

      val header_fn = (p.options.get(_.depth) + 1) match {
        case 1 => <.h1
        case 2 => <.h2
        case 3 => <.h3
        case 4 => <.h4
      }

      <.div(^.className:= "wrapper wrapper-white",
        header_fn(p.title),
        grid.toSeq.toTagMod
      )
    }

  }

  val component = ScalaComponent.builder[Props]("ColumnsLayoutComponent")
    .render_PC((p, c) => (new Builder).render(p, c))
    .build

  override def apply(title: String, children: Seq[GroupChildElement], options: FormRenderingOptions): Unmounted =
    component(Props(title, children, options))(children.map(_.child): _*)

}


