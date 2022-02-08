package sysmo.coviddata.components.forms

import japgolly.scalajs.react.vdom.{TagMod, VdomElement, VdomNode}

trait FormLayout {
  def to_component(field_editors: Seq[VdomElement]): VdomNode
}


object ColumnarLayoutComponent {
  import japgolly.scalajs.react._
  import japgolly.scalajs.react.vdom.html_<^._
  case class Props(num_columns : Int)

  val component = ScalaComponent.builder[Props]("FormLayout")
    .render_PC((p, c) => {
      val col_width = 12 / p.num_columns - 1
      <.div(^.className:="wrapper wrapper-white", c.toList.grouped(p.num_columns).map(g =>
        <.div(^.className:="row",
          g.map(x => <.div(^.className:= f"col-md-$col_width", x)).toTagMod)

      ).toTagMod)}
    )
    .build

  def apply(num_columns : Int)(field_editors : Seq[VdomElement]) =
    component(Props(num_columns))(field_editors:_*)
}

case class ColumnarLayout(num_columns : Int) extends FormLayout {
  def to_component(field_editors: Seq[VdomElement]): VdomNode =
    ColumnarLayoutComponent.apply(num_columns)(field_editors)
}