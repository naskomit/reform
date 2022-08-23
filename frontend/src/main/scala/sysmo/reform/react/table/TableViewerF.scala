package sysmo.reform.react.table

import sysmo.reform.react.ReactComponent
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.react.table.aggrid.{AgGridComponent, AgGridFacades => AGF}
import sysmo.reform.shared.data.Value
import Value.implicits._
import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.shared.table.{QuerySource, SelectionHandler, Table, TableService}
import sysmo.reform.shared.types.AtomicDataType

import java.util.Date

//(implicit f2c: F2Callback[F])
class TableViewerF[F[+_]] extends ReactComponent {
  case class Props(ts: TableService[F], schema: Table.Schema, source: QuerySource,
                   height: String, selection_handler: Option[SelectionHandler])

  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def schema2columns(schema: Table.Schema): Seq[AGF.ColumnProps] = {
      schema.fields.map(field => {
        val filter = field.dtype match {
          case  AtomicDataType.Char => Some(AGF.Filters.text)
          case AtomicDataType.Int => Some(AGF.Filters.number)
          case AtomicDataType.Real => Some(AGF.Filters.number)
          case _ => None
        }

        val value_getter: AGF.ValueGetter = (x : AGF.ValueGetterParams) => {
          val NA: String = "<N/A>"
          x.data.toOption match {
            case Some(Some(row_data)) => row_data.get(field.name) match {
              case y if y.is_na => NA
              case y: Value.DateValue => {
                y.get[Date].map(date =>
                  (new scala.scalajs.js.Date(date.getTime)).toDateString()
                ).getOrElse[String](NA)
              }
              case y => y.get[String].getOrElse[String](NA)
            }
            case _ => NA
          }
        }

        AGF.column(
          field.name,
          value_getter = Some(value_getter),
          headerName = Some(field.make_descr),
          filter = filter, sortable = Some(true)
        )
      })
    }

    def render (p: Props, s: State): VdomElement = {
      <.div(
        AgGridComponent(
          p.ts, p.source, p.schema,
          schema2columns(p.schema),
          p.height,
          p.selection_handler
        )
      )
    }
  }

  val component =
    ScalaComponent.builder[Props]("RecordTableViewer")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(ts : TableService[F], schema: Table.Schema, source: QuerySource,
            height: String = "800px", selection_handler: Option[SelectionHandler]): Unmounted = {
    component(Props(ts, schema, source, height, selection_handler))
  }
}
