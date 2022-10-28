package sysmo.reform.react.table

import sysmo.reform.react.ReactComponent
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.react.table.aggrid.{AgGridComponent, CellRenderer, DateCellRenderer, IdCellRenderer, TextCellRenderer, AgGridFacades => AGF}
import sysmo.reform.shared.data.Value
import Value.implicits._
import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.shared.query.QuerySource
import sysmo.reform.shared.table.{SelectionHandler, Table, TableService}
import sysmo.reform.shared.types.PrimitiveDataType

class TableViewerF[F[+_]] extends ReactComponent {
  case class Props(ts: TableService[F], schema: Table.Schema, source: QuerySource,
                   height: String, selection_handler: Option[SelectionHandler])

  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def schema2columns(schema: Table.Schema): Seq[AGF.ColumnProps] = {
      schema.fields.map(field => {
        val filter = field.dtype match {
          case  PrimitiveDataType.Char => Some(AGF.Filters.text)
          case PrimitiveDataType.Int => Some(AGF.Filters.number)
          case PrimitiveDataType.Real => Some(AGF.Filters.number)
          case _ => None
        }

        val value_getter: AGF.ValueGetter = (x : AGF.ValueGetterParams) => {
          x.data.toOption.flatten match {
            case Some(row_data) => row_data.get(field.name)
            case None => Value.empty
          }
        }

        val cell_renderer = field.dtype match {
          case PrimitiveDataType.Date => DateCellRenderer
          case PrimitiveDataType.Id => IdCellRenderer
          case _ => TextCellRenderer
        }


        AGF.column(
          field.name,
          value_getter = Some(value_getter),
          headerName = Some(field.make_descr),
          filter = filter, sortable = Some(true),
          cell_renderer = Some(cell_renderer)
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
