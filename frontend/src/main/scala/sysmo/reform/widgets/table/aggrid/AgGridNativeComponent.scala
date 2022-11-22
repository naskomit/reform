package sysmo.reform.widgets.table.aggrid

import japgolly.scalajs.react.{Children, JsComponent}
import org.scalajs.dom
import sysmo.reform.effects.ActionManager
import sysmo.reform.widgets.table.{CellRenderer, ColumnFilter, ColumnOptions, TableOptions, TextCellFormatter}
import sysmo.reform.widgets.table.aggrid.{AgGridFacades => Fc}
import sysmo.reform.shared.actions.Action
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.logging.Logging
import sysmo.reform.shared.table.{SelectionHandler, Table}
import sysmo.reform.widgets.tooltip.TableCellTooltip

import scala.scalajs.js.JSConverters._
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

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
    var onCellClicked: js.UndefOr[js.Function1[Fc.CellClickedEvent, Unit]] = js.native
    var onCellContextMenu: js.Function1[Fc.CellContextMenuEvent, Unit] = js.native

    var tooltipShowDelay: js.UndefOr[Int] = js.native
    var tooltipHideDelay: js.UndefOr[Int] = js.native
  }

  val component = JsComponent[Props, Children.None, Null](AgGridReact)

  def install_click_handler(col_opt: Fc.ColumnProps, f: Value => Action, action_manager: ActionManager): Unit = {
    val onCellClicked: js.Function1[Fc.CellClickedEvent, Unit] = (event: Fc.CellClickedEvent) => {
//      println("Cell clicked")
//      println(event.column.getColId())
//      println(s"(${event.rowIndex}, ${event.column.getColId()})")
//      println(event.value)
      action_manager.dispatch(f(event.value))
    }

    col_opt.onCellClicked = Some(onCellClicked).orUndefined
  }

  def install_tooltip(col_opt: Fc.ColumnProps): Unit = {
    import Value.implicits._
    val tooltip_value_getter: Fc.TooltipValueGetter =
      (props) => {
        props.value.get[String].getOrElse("")
      }

    col_opt.tooltipValueGetter = Some(tooltip_value_getter).orUndefined

    //      val tooltipComponent = ScalaComponent.builder[js.Any]
    //        .stateless
    //        .render_P(p => <.div("Hello"))
    //        .build


    //      val tooltip: Fc.ReactComponentFn[js.Any] =
    //        (props) => {
    //          tooltipComponent(props).raw
    //        }
    //      col_js.tooltipComponent = Some(tooltip).orUndefined

  }

  def create_columns(column_options: Seq[ColumnOptions], panel_manager: ActionManager): Seq[Fc.ColumnProps] = {
    column_options.map{ col_opt =>
      val col_js = (new js.Object).asInstanceOf[Fc.ColumnProps]
      col_js.field = col_opt.id
      col_js.headerName = col_opt.header_name.orUndefined
      val value_getter: Fc.ValueGetter = (x : Fc.ValueGetterParams) => {
        x.data.toOption.flatten match {
          case Some(row_data) => row_data.get(col_opt.id)
          case None => Value.empty
        }
      }
      col_js.valueGetter = value_getter
      col_js.filter = col_opt.filter match {
        case None => None.orUndefined
        case Some(ColumnFilter.Default) => true
        case Some(ColumnFilter.Text) => "agTextColumnFilter"
        case Some(ColumnFilter.Number) => "agNumberColumnFilter"
      }
      col_js.sortable = col_opt.sortable.orUndefined
      col_js.resizable = true

      val react_cell_renderer: Option[Fc.ReactCellRenderer] =
        Some {
          (props) => CellRenderer(
            col_opt.cell_formatter.getOrElse(TextCellFormatter), col_opt.cell_actions
          )(props.value).raw
        }

      col_js.cellRendererFramework =  react_cell_renderer.orUndefined

      // Install click handler
      col_opt.cell_actions.click match {
        case Some(f) => install_click_handler(col_js, f, panel_manager)
        case None =>
      }

      // Install tooltip
      col_opt.tooltip match {
        case Some(true) => install_tooltip(col_js)
        case _ =>
      }

      col_js
    }
  }

  /** Constructor */
  def apply(datasource: Fc.TableDatasource, options: TableOptions, action_manager: ActionManager) = {
    val p = (new js.Object).asInstanceOf[Props]
    var api: Option[Fc.API] = None
    var column_api: Option[Fc.ColumnAPI] = None

    p.reactUi = true

    val grid_options = (new js.Object).asInstanceOf[Fc.GridOptions]
    grid_options.rowModelType = "infinite"
    grid_options.datasource = datasource
    p.gridOptions = grid_options
    p.columnDefs = create_columns(options.column_options, action_manager).toJSArray

    val onGridReady: js.Function1[Fc.OnGridReady, Unit] = (params: Fc.OnGridReady) => {
      api = Some(params.api)
      params.api.sizeColumnsToFit()
      column_api = Some(params.columnApi)
    }
    p.onGridReady = Some(onGridReady).orUndefined

    p.tooltipShowDelay = 100
    p.tooltipHideDelay = 100
//    def install_selection_handler(handler: SelectionHandler): Unit = {
//      logger.info(s"Installing selection handler ${handler.mode}")
//      val onSelectionChanged: js.Function0[Unit] = () => {
//        val selection = api.get.getSelectedRows.bind(api.get)()
//          .asInstanceOf[js.Array[Option[Table.Row]]]
//          .toSeq.collect {
//          case Some(row: Table.Row) => row
//        }
//        handler.on_change(selection)
//      }
//      p.onSelectionChanged = Some(onSelectionChanged).orUndefined
//    }

//    def install_click_handler(): Unit = {
//      val onCellClicked: js.Function1[Fc.CellClickedEvent, Unit] = (event: Fc.CellClickedEvent) => {
//        println("Cell clicked")
//        println(event.column.getColId())
//        println(s"(${event.rowIndex}, ${event.column.getColId()})")
//        println(event.value)
//      }
//
//      p.onCellClicked = Some(onCellClicked).orUndefined
//
////      p.onCellContextMenu = (event: Fc.CellContextMenuEvent) => {
////        println("Cell context menu")
////        println(s"(${event.rowIndex}, ${event.column.getColId()})")
////      }
//    }

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

