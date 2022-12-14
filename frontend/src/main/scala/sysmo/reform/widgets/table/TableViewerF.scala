package sysmo.reform.widgets.table

import sysmo.reform.widgets.ReactComponent
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.effects.ActionManager
import sysmo.reform.widgets.table.aggrid.{AgGridNativeComponent, AgGridSourceAdaptor, AgGridFacades => AGF}
import sysmo.reform.shared.query.QuerySource
import sysmo.reform.shared.table.{Table, TableService}

class TableViewerF[F[+_]] extends ReactComponent {
  case class Props(ts: TableService[F], schema: Table.Schema,
                   source: QuerySource, table_options: TableOptions)

  case class State()

  final class Backend($: BackendScope[Props, State]) {


    def render (p: Props, s: State): VdomElement = {
      val data_source = AgGridSourceAdaptor(p.ts, p.source, p.schema)
      val opts = p.table_options
      ActionManager.ctx.consume(action_manager =>
        <.div(
          ^.cls := "ag-theme-alpine",
          ^.width := opts.size.width,
          ^.height := opts.size.height,
          AgGridNativeComponent(data_source.native, p.table_options, action_manager)
        )
      )
    }
  }

  val component =
    ScalaComponent.builder[Props]("TableViewer")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(ts : TableService[F], schema: Table.Schema,
            source: QuerySource, table_options: TableOptions): Unmounted = {
    component(Props(ts, schema, source, table_options))
  }
}
