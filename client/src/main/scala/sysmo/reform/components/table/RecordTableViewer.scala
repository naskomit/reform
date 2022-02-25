package sysmo.reform.components.table

import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js
import sysmo.reform.shared.data.{IntegerType, RecordField, RecordWithMeta, StringType}
import sysmo.reform.shared.{query => Q}
import sysmo.reform.components.table.aggrid.AgGridComponent
import sysmo.reform.components.table.aggrid.{AgGridFacades => agf}
import sysmo.reform.data.TableDatasource

object RecordTableViewer {
  import japgolly.scalajs.react._

  case class Props[U](ds : TableDatasource[U], source: Q.QuerySource,columns : Seq[RecordField])
  case class State()

  final class Backend[U]($: BackendScope[Props[U], State]) {
    def render (p: Props[U], s: State): VdomElement = {
      <.div(^.style:=js.Dictionary("marginLeft" -> "100px", "marginRight" -> "100px"),
        AgGridComponent(
          p.ds, p.source,
          p.columns.map(col => {
            val filter = col.tpe match {
              case StringType() => Some(agf.Filters.text)
              case IntegerType() => Some(agf.Filters.number)
              case _ => None
            }
            agf.column(col.name, headerName = Some(col.label), filter = filter, sortable = Some(true))
          })
        )
      )
    }
  }

  def component[U] =
    ScalaComponent.builder[Props[U]]("RecordTableViewer")
    .initialState(State())
    .renderBackend[Backend[U]]
    .build

  def apply[U](ds : TableDatasource[U], table : String)(implicit meta_holder: RecordWithMeta[U]) = {
    val meta = meta_holder._meta
    val columns = meta.field_keys.map(x => meta.fields(x))
    val source = Q.SingleTable(table)
    component[U](Props(ds, source, columns))
  }
}
