package sysmo.reform.components.forms4.layouts

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, VdomNode}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.forms4.options.FormRenderingOptions

import scala.collection.mutable

case class ColumnsLayout(num_columns : Int) extends FormGroupLayout {
  override def apply(title: String, children: Seq[ChildElement], options: FormRenderingOptions): VdomNode =
    ColumnsLayoutComponent.component(
      ColumnsLayoutComponent.Props(this, title, children, options))(children.map(_.child): _*)
}

case class Builder(p: ColumnsLayoutComponent.Props, children: PropsChildren) {
  private val row_nodes: mutable.ArrayBuffer[VdomNode] = mutable.ArrayBuffer()
  private var row_filled_width = 0.0
  private val grid: mutable.ArrayBuffer[VdomElement] = mutable.ArrayBuffer()
  private var row_index = 0
  private var elem_index = 0
  private val base_col_width = 0.25

  def build_row: Unit = {
    val key = row_index.toString
    grid += <.div(^.className:= f"row $key", ^.key:= key, row_nodes.toTagMod)
    row_index += 1
    elem_index = 0
    row_filled_width = 0.0
    row_nodes.clear()
  }

  def compute_width(elem: ChildElement): Double  = {
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

  def build_grid : VdomNode = {
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

    <.div(^.className:="wrapper wrapper-white",
      header_fn(p.title),
      grid.toSeq.toTagMod
    )
  }

}


object ColumnsLayoutComponent {
  case class Props(layout: ColumnsLayout, title: String, child_elements: Seq[ChildElement], options: FormRenderingOptions)

  val component = ScalaComponent.builder[Props]("ColumnarFormLayout")
    .render_PC((p, c) => Builder(p, c).build_grid)
    .build

//  def apply(layout: ColumnsLayout)(children : Seq[ChildElement]) =
//    component(layout)(children: _*)
}

