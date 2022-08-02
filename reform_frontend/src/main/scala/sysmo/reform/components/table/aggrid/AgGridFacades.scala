package sysmo.reform.components.table.aggrid

import org.scalajs.dom
import sysmo.reform.shared.{expr => E}
import sysmo.reform.shared.{query => Q}

import scala.scalajs.js
import js.JSConverters._
import scala.scalajs.js.annotation.JSImport
import japgolly.scalajs.react.{Children, JsComponent}
import sysmo.reform.components.table.TableSelectionHandler

import scala.scalajs.js.{UndefOr, |}
import scalajs.js.annotation.JSGlobal
import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.util.log.Logging

object AgGridFacades extends Logging {
  @js.native
  trait GridOptions extends js.Object {
    var rowModelType: String = js.native
    var datasource: TableDatasource  = js.native
  }

  type FilterModel = js.Dictionary[ColumnFilterJS]
  type SortModel = js.Array[SortModelItem]

  @js.native
  trait ColumnFilterJS extends js.Object {
    val filterType: js.UndefOr[String] = js.native
    val operator: js.UndefOr[String] = js.native
    val `type`: js.UndefOr[String] = js.native
  }

  @js.native
  trait BinaryFilterJS extends ColumnFilterJS {
    val condition1: ColumnFilterJS = js.native
    val condition2: ColumnFilterJS = js.native
  }

  @js.native
  trait TextFilterModelJS extends ColumnFilterJS {
    val filter: String = js.native
    val filterTo: js.UndefOr[String] = js.native
  }

  @js.native
  trait NumberFilterModelJS extends ColumnFilterJS {
    val filter: Double = js.native
    val filterTo: Double = js.native
  }

  @js.native
  trait DateFilterModelJS extends ColumnFilterJS {
    val dateFrom: String = js.native
    val dateTo: String = js.native
  }

  case class ColumnFilter(filter_type: Option[String], operator: Option[String], tpe: Option[String])
  object ColumnFilter {
    def fromJS(flt_js: ColumnFilterJS): ColumnFilter =
      ColumnFilter(flt_js.filterType.toOption, flt_js.operator.toOption, flt_js.`type`.toOption)
  }


  def extract_filter(flt_js : ColumnFilterJS, column: String): Option[E.PredicateExpression] = {
    import E.{Expression => EX}
      val flt = ColumnFilter.fromJS(flt_js)
      dom.console.log(flt_js)
      flt match {
        case ColumnFilter(_, Some(op), None) => {
          val flt_binary = flt_js.asInstanceOf[BinaryFilterJS]
          val cond1 = extract_filter(flt_binary.condition1, column)
          val cond2 = extract_filter(flt_binary.condition2, column)
          (op, cond1, cond2) match {
            case ("AND", Some(c1), Some(c2)) => Some(E.LogicalAnd(c1, c2))
            case ("OR", Some(c1), Some(c2)) => Some(E.LogicalOr(c1, c2))
            case _ => {logger.warn(s"Cannot decode filter ${(op, cond1, cond2)}"); None}
          }
        }

        case ColumnFilter(Some("text"), None, Some(pred_str)) => {
          val f = flt_js.asInstanceOf[TextFilterModelJS]
          val predicate = pred_str match {
            case "equals" => EX.col(column) === EX(f.filter)
            case "notEqual" => EX.col(column) !== EX(f.filter)
            case "contains" => EX.col(column).str.containing(EX(f.filter))
            case "notContains" => EX.col(column).str.not_containing(EX(f.filter))
            case "startsWith" => EX.col(column).str.starting_with(EX(f.filter))
            case "endsWith" => EX.col(column).str.ending_with(EX(f.filter))

          }
          Some(predicate)
        }

        case ColumnFilter(Some("number"), None, Some(pred_str)) => {
          val f = flt_js.asInstanceOf[NumberFilterModelJS]
          if (pred_str == "inRange") {
            Some(E.LogicalAnd(
              E.NumericalPredicate(E.NumericalPredicateOp.>=, E.ColumnRef(column), E.Constant(f.filter)),
              E.NumericalPredicate(E.NumericalPredicateOp.<=, E.ColumnRef(column), E.Constant(f.filterTo))
            ))
          } else {
            val predicate = pred_str match {
              case "equals" => EX.col(column) === EX(f.filter)
              case "notEqual" => EX.col(column) !== EX(f.filter)
              case "lessThan" => EX.col(column) < EX(f.filter)
              case "lessThanOrEqual" => EX.col(column) <= EX(f.filter)
              case "greaterThan" => EX.col(column) > EX(f.filter)
              case "greaterThanOrEqual" => EX.col(column) >= EX(f.filter)

            }
            Some(predicate)
          }
        }

        case ColumnFilter(Some("date"), None, pred) => {
          None

        }

        case _ => {logger.warn(s"Cannot decode filter $flt") ;None}
      }
  }

  @js.native
  trait SortModelItem extends js.Object {
    val colId: String = js.native
    val sort: String = js.native
  }

  @js.native
  trait IGetRowsParams extends js.Object {

    /** The first row index to get. */
    val startRow: Int = js.native
    /** The first row index to NOT get. */
    val endRow: Int = js.native
    /** Callback to call for the result when successful. */
    val successCallback: js.Function2[js.Array[_] | Proxy, Int, _] = js.native
    /** Callback to call when the request fails. */
    val failCallback: js.Function0[Unit] = js.native
    /** If doing server side sorting, contains the sort model */
    val sortModel: SortModel = js.native
    /** If doing server side filtering, contains the filter model */
    val filterModel: FilterModel = js.native
    /** The context as provided on `gridOptions.context` */
    val context: js.Any = js.native
  }

  @js.native
  trait TableDatasource extends js.Object {
//    var rowCount: js.Function0[Int] = js.native
    var getRows: js.Function1[IGetRowsParams, _] = js.native
  }

  @js.native
  trait RowNode extends js.Object {
    val rowIndex: Int = js.native
  }

  trait Column extends js.Object {

  }

  @js.native
  trait ValueGetterParams extends js.Object {
    val node: RowNode = js.native
    val data: js.UndefOr[Option[sdt.Row]] = js.native
    val column: Column = js.native
  }

  type ValueGetter = js.Function1[ValueGetterParams, js.Any]

  @js.native
  trait ColumnProps extends js.Object {
    var field: String = js.native
    var valueGetter: js.UndefOr[ValueGetter] = js.native
    //      var `type`: String | js.Array[String]
    var headerName: js.UndefOr[String] = js.native
    var filter: js.UndefOr[String | Boolean] = js.native
    var sortable: js.UndefOr[Boolean] = js.native
  }

  object Filters extends Enumeration {
    val default, text, number = Value
  }

  def column(field: String,
             value_getter: Option[ValueGetter] = None,
             headerName: Option[String] = None,
             filter: Option[Filters.Value] = None,
             sortable: Option[Boolean] = None
            ) : ColumnProps = {
    val col_js = (new js.Object).asInstanceOf[ColumnProps]
    col_js.field = field
    col_js.valueGetter = value_getter.orUndefined
    col_js.headerName = headerName.orUndefined
    col_js.filter = filter match {
      case None => None.orUndefined
      case Some(Filters.default) => true
      case Some(Filters.text) => "agTextColumnFilter"
      case Some(Filters.number) => "agNumberColumnFilter"
    }
    col_js.sortable = sortable.orUndefined

    col_js
  }

  @js.native
  @JSGlobal
  class Proxy(target: Any, handler: js.Any) extends js.Object

  def table_proxy(table: sdt.Table): Proxy = {
    val getter : js.Function3[sdt.Table, String, js.Any, Option[sdt.Row]] =
      (table: sdt.Table, prop: String, receiver: js.Any) => {
        val index = prop.toInt
        if (index < table.nrow) {
          Some(table.row(index))
        }
        else {
          None
        }
      }
    val table_proxy_handler = js.Dynamic.literal(
      get = getter
    )

    new Proxy(table, table_proxy_handler)
  }

  @js.native
  trait API extends js.Object {
    val getSelectedRows: js.Function0[js.Any] = js.native
    val getSelectedNodes: js.Function0[js.Any] = js.native
  }

  @js.native
  trait ColumnAPI extends js.Object {

  }

  @js.native
  trait OnGridReady extends js.Object {
    val api: API = js.native
    val columnApi: ColumnAPI = js.native
  }

  object AgGridNativeComponent {

    @JSImport("ag-grid-react", "AgGridReact")
    @js.native
    object AgGridReact extends js.Object


    @js.native
    trait Props extends js.Object {
      var columnDefs: js.Array[ColumnProps] = js.native
      var rowData: js.Object = js.native
      var reactUi: Boolean = js.native
      var gridOptions: GridOptions = js.native
      var rowSelection: js.UndefOr[String] = js.native
      var onSelectionChanged: js.UndefOr[js.Function0[Unit]] = js.native
      var onGridReady: js.UndefOr[js.Function1[OnGridReady, Unit]] = js.native
    }

    val component = JsComponent[Props, Children.None, Null](AgGridReact)

    def apply(datasource: TableDatasource, columns : Seq[ColumnProps],
              selection_handler: Option[TableSelectionHandler]) = {
      val p = (new js.Object).asInstanceOf[Props]
      var api: Option[API] = None
      var column_api: Option[ColumnAPI] = None

      p.reactUi = true

      val grid_options = (new js.Object).asInstanceOf[GridOptions]
      grid_options.rowModelType = "infinite"
      grid_options.datasource = datasource
      p.gridOptions = grid_options
      p.columnDefs = columns.toJSArray

      val onGridReady: js.Function1[OnGridReady, Unit] = (params: OnGridReady) => {
        api = Some(params.api)
        column_api = Some(params.columnApi)
      }
      p.onGridReady = Some(onGridReady).orUndefined

      def install_handler(handler: TableSelectionHandler): Unit = {
        println(s"Installing handler ${handler.mode}")
        val onSelectionChanged: js.Function0[Unit] = () => {
          val selection = api.get.getSelectedRows.bind(api.get)()
            .asInstanceOf[js.Array[Option[sdt.Row]]]
            .toSeq.collect {
              case Some(row: sdt.Row) => row
            }
          handler.on_change(selection)
        }
        p.onSelectionChanged = Some(onSelectionChanged).orUndefined
      }

      selection_handler match {
        case Some(handler) => handler.mode match {
          case TableSelectionHandler.NoSelection =>
          case TableSelectionHandler.SingleRow => {
            p.rowSelection = Some("single").orUndefined
            install_handler(handler)
          }
          case TableSelectionHandler.MultiRow => {
            p.rowSelection = Some("multiple").orUndefined
            install_handler(handler)
          }
        }
        case None =>
      }

      component.withProps(p)()
    }
  }
}
