package sysmo.reform.react.table.aggrid

import japgolly.scalajs.react.{Children, JsComponent}
import sysmo.reform.react.table.{ColumnOptions, ColumnFilter, TableOptions}
import sysmo.reform.react.table.aggrid.{AgGridFacades => Fc}
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.logging.Logging
import sysmo.reform.shared.table.{SelectionHandler, Table}

import scala.scalajs.js.JSConverters._
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import Value.implicits._

object AgGridNativeComponent extends Logging {

  @JSImport("ag-grid-react", "AgGridReact")
  @js.native
  object AgGridReact extends js.Object

  /** The Main props */

  @js.native
  trait Props extends js.Object {
    var columnDefs: js.Array[Fc.ColumnProps] = js.native
    var rowData: js.Object = js.native
    var reactUi: Boolean = js.native
    var gridOptions: Fc.GridOptions = js.native
    var rowSelection: js.UndefOr[String] = js.native
    var onSelectionChanged: js.UndefOr[js.Function0[Unit]] = js.native
    var onGridReady: js.UndefOr[js.Function1[Fc.OnGridReady, Unit]] = js.native
    var onCellClicked: js.Function1[Fc.CellClickedEvent, Unit] = js.native
    var onCellContextMenu: js.Function1[Fc.CellContextMenuEvent, Unit] = js.native
  }

  val component = JsComponent[Props, Children.None, Null](AgGridReact)


  def create_columns(column_options: Seq[ColumnOptions]): Seq[Fc.ColumnProps] = {
    column_options.map{col_option =>
      val col_js = (new js.Object).asInstanceOf[Fc.ColumnProps]
      col_js.field = col_option.id
      col_js.headerName = col_option.header_name.orUndefined
      val value_getter: Fc.ValueGetter = (x : Fc.ValueGetterParams) => {
        x.data.toOption.flatten match {
          case Some(row_data) => row_data.get(col_option.id)
          case None => Value.empty
        }
      }
      col_js.valueGetter = value_getter
      col_js.filter = col_option.filter match {
        case None => None.orUndefined
        case Some(ColumnFilter.Default) => true
        case Some(ColumnFilter.Text) => "agTextColumnFilter"
        case Some(ColumnFilter.Number) => "agNumberColumnFilter"
      }
      col_js.sortable = col_option.sortable.orUndefined

      val cell_renderer: Option[Fc.JSCellRenderer] = col_option.cell_renderer.map {renderer =>
        (params: Fc.ICellRendererParams) => {
          val v: Value = params.value.asInstanceOf[Value]
          renderer.render(v)
        }
      }

      col_js.cellRenderer = cell_renderer.orUndefined

      col_js
    }
  }

  /** Constructor */
  def apply(datasource: Fc.TableDatasource, options: TableOptions) = {
    val p = (new js.Object).asInstanceOf[Props]
    var api: Option[Fc.API] = None
    var column_api: Option[Fc.ColumnAPI] = None

    p.reactUi = true

    val grid_options = (new js.Object).asInstanceOf[Fc.GridOptions]
    grid_options.rowModelType = "infinite"
    grid_options.datasource = datasource
    p.gridOptions = grid_options
    p.columnDefs = create_columns(options.column_options).toJSArray

    val onGridReady: js.Function1[Fc.OnGridReady, Unit] = (params: Fc.OnGridReady) => {
      api = Some(params.api)
      column_api = Some(params.columnApi)
    }
    p.onGridReady = Some(onGridReady).orUndefined

    def install_selection_handler(handler: SelectionHandler): Unit = {
      logger.info(s"Installing selection handler ${handler.mode}")
      val onSelectionChanged: js.Function0[Unit] = () => {
        val selection = api.get.getSelectedRows.bind(api.get)()
          .asInstanceOf[js.Array[Option[Table.Row]]]
          .toSeq.collect {
          case Some(row: Table.Row) => row
        }
        handler.on_change(selection)
      }
      p.onSelectionChanged = Some(onSelectionChanged).orUndefined
    }

    def install_event_handlers(): Unit = {
      p.onCellClicked = (event: Fc.CellClickedEvent) => {
        println("Cell clicked")
        println(event.column.getColId())
        println(s"(${event.rowIndex}, ${event.column.getColId()})")
        println(event.data)
        println(event.value)
      }

      p.onCellContextMenu = (event: Fc.CellContextMenuEvent) => {
        println("Cell context menu")
        println(s"(${event.rowIndex}, ${event.column.getColId()})")
      }
    }

//    selection_handler match {
//      case Some(handler) => handler.mode match {
//        case SelectionHandler.NoSelection =>
//        case SelectionHandler.SingleRow => {
//          p.rowSelection = Some("single").orUndefined
//          install_selection_handler(handler)
//        }
//        case SelectionHandler.MultiRow => {
//          p.rowSelection = Some("multiple").orUndefined
//          install_selection_handler(handler)
//        }
//      }
//      case None =>
//    }
    // TODO ???
//    install_event_handlers()

    component.withProps(p)()
  }
}
