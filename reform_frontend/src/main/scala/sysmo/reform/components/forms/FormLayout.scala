package sysmo.reform.components.forms

import japgolly.scalajs.react.vdom.{TagMod, VdomElement, VdomNode}

import scala.collection.mutable

trait FormLayout {
  def to_component(field_editors: Seq[VdomElement]): VdomNode
}



object ColumnarLayoutComponent {
  import japgolly.scalajs.react._
  import japgolly.scalajs.react.vdom.html_<^._
  case class Props(num_columns : Int)

  def build_row(nodes: Seq[VdomElement], key: String): VdomElement = {
    <.div(^.className:= f"row $key", ^.key:= key, nodes.toTagMod)
  }

  def build_grid(children: PropsChildren, num_columns : Int) : TagMod = {
    val col_width = 12 / num_columns - 1
    val row_seq = mutable.ArrayBuffer[VdomElement]()
    var grid = mutable.ArrayBuffer[VdomElement]()
    var row_index = 0
    for ((child, index )<- children.toList.zipWithIndex) {
      val cell = <.div(^.className:= f"col-md-$col_width $index", ^.key:= index, child)
      row_seq += cell
      if (row_seq.length >= num_columns) {
        grid += build_row(row_seq.toSeq, row_index.toString)
        row_index += 1
        row_seq.clear()
      }
    }

    if (row_seq.nonEmpty) {
      grid += build_row(row_seq.toSeq, row_index.toString)
      row_index += 1
      row_seq.clear()
    }

    grid.toTagMod
  }

  val component = ScalaComponent.builder[Props]("ColumnarFormLayout")
    .render_PC((p, c) =>
      <.div(^.className:="wrapper wrapper-white", build_grid(c, p.num_columns))
    )
//    .componentDidMount(f => Callback {})
    .build

  def apply(num_columns : Int)(field_editors : Seq[VdomElement]) =
    component(Props(num_columns))(field_editors:_*)
}

case class ColumnarLayout(num_columns : Int) extends FormLayout {
  def to_component(field_editors: Seq[VdomElement]): VdomNode =
    ColumnarLayoutComponent.apply(num_columns)(field_editors)
}