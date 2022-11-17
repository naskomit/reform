package sysmo.reform.react.table

import sysmo.reform.react.ReactComponent
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.react.table.aggrid.{AgGridNativeComponent, AgGridSourceAdaptor, AgGridFacades => AGF}
import sysmo.reform.shared.data.Value
import Value.implicits._
import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.shared.query.QuerySource
import sysmo.reform.shared.table.{SelectionHandler, Table, TableService}
import sysmo.reform.shared.types.PrimitiveDataType

class TableViewerF[F[+_]] extends ReactComponent {
  case class Props(ts: TableService[F], schema: Table.Schema,
                   source: QuerySource, table_options: TableOptions)

  case class State()

  final class Backend($: BackendScope[Props, State]) {


    def render (p: Props, s: State): VdomElement = {
      val data_source = AgGridSourceAdaptor(p.ts, p.source, p.schema)
      val opts = p.table_options

      <.div(
        ^.cls := "ag-theme-alpine",
        ^.width := opts.size.width,
        ^.height := opts.size.height,
        AgGridNativeComponent(data_source.native, p.table_options)
      )
    }
  }

  val component =
    ScalaComponent.builder[Props]("RecordTableViewer")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(ts : TableService[F], schema: Table.Schema,
            source: QuerySource, table_options: TableOptions): Unmounted = {
    component(Props(ts, schema, source, table_options))
  }
}
