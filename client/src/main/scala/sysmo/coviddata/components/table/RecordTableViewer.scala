package sysmo.coviddata.components.table

import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js
import sysmo.reform.shared.data.{RecordField, RecordWithMeta}

object RecordTableViewer {
  import japgolly.scalajs.react._

  case class Props[U](ds : TableDatasource[U], columns : Seq[RecordField])
  case class State()

  final class Backend[U]($: BackendScope[Props[U], State]) {
    def render (p: Props[U], s: State): VdomElement = {
      <.div(^.style:=js.Dictionary("marginLeft" -> "100px", "marginRight" -> "100px"),
        AgGridComponent(
          p.ds,
          p.columns.map(col => AgGridColumn(col.name, col.label))
      ))

    }
  }

  def component[U] =
    ScalaComponent.builder[Props[U]]("RecordTableViewer")
    .initialState(State())
    .renderBackend[Backend[U]]
    .build

  def apply[U](ds : TableDatasource[U])(implicit meta_holder: RecordWithMeta[U]) = {
    val meta = meta_holder._meta
    val columns = meta.field_keys.map(x => meta.fields(x))
    component[U](Props(ds, columns))
  }
}
