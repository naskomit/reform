package sysmo.reform.react.table.aggrid

import org.scalajs.dom
import sysmo.reform.shared.expr.{PredicateExpression, Expression => E}
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.logging.Logging
import sysmo.reform.shared.table.{Table}

import scala.scalajs.js
import scala.scalajs.js.|

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


  def extract_filter(flt_js : ColumnFilterJS, column: String): Option[PredicateExpression] = {
    import E.implicits._
    import Value.implicits._
    val flt = ColumnFilter.fromJS(flt_js)
      dom.console.log(flt_js)
      flt match {
        case ColumnFilter(_, Some(op), None) => {
          val flt_binary = flt_js.asInstanceOf[BinaryFilterJS]
          val cond1 = extract_filter(flt_binary.condition1, column)
          val cond2 = extract_filter(flt_binary.condition2, column)
          (op, cond1, cond2) match {
            case ("AND", Some(c1), Some(c2)) => Some(c1 && c2)
            case ("OR", Some(c1), Some(c2)) => Some(c1 || c2)
            case _ => {logger.warn(s"Cannot decode filter ${(op, cond1, cond2)}"); None}
          }
        }

        case ColumnFilter(Some("text"), None, Some(pred_str)) => {
          val f = flt_js.asInstanceOf[TextFilterModelJS]
          val predicate = pred_str match {
            case "equals" => E.field(column) === E(f.filter)
            case "notEqual" => E.field(column) !== E(f.filter)
            case "contains" => E.field(column).str.containing(E(f.filter))
            case "notContains" => E.field(column).str.not_containing(E(f.filter))
            case "startsWith" => E.field(column).str.starting_with(E(f.filter))
            case "endsWith" => E.field(column).str.ending_with(E(f.filter))

          }
          Some(predicate)
        }

        case ColumnFilter(Some("number"), None, Some(pred_str)) => {
          val f = flt_js.asInstanceOf[NumberFilterModelJS]
          val predicate: PredicateExpression = if (pred_str == "inRange") {
            E.field(column) >= E(f.filter) && E.field(column) <= E(f.filterTo)
          } else {
            pred_str match {
              case "equals" => E.field(column) === E(f.filter)
              case "notEqual" => E.field(column) !== E(f.filter)
              case "lessThan" => E.field(column) < E(f.filter)
              case "lessThanOrEqual" => E.field(column) <= E(f.filter)
              case "greaterThan" => E.field(column) > E(f.filter)
              case "greaterThanOrEqual" => E.field(column) >= E(f.filter)

            }
          }
          Some(predicate)
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
    val successCallback: js.Function2[js.Array[_] | js.Object, Int, _] = js.native
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

  @js.native
  trait Column extends js.Object {
    def getColId(): String = js.native
  }

  @js.native
  trait ValueGetterParams extends js.Object {
    val node: RowNode = js.native
    val data: js.UndefOr[Option[Table.Row]] = js.native
    val column: Column = js.native
  }

  type ValueGetter = js.Function1[ValueGetterParams, Value]

  @js.native
  trait ICellRendererParams extends js.Object {
    val value: Value = js.native
    val valueFormatted: Any = js.native
  }
  type ReactElement = japgolly.scalajs.react.facade.React.Element
  type ReactComponentFn[T] = (T) => ReactElement
  type JSCellRenderer = js.Function1[ICellRendererParams, js.Object]
  type ReactCellRenderer = js.Function1[ICellRendererParams, ReactElement]

  @js.native
  trait ColumnProps extends js.Object {
    var field: String = js.native
    var valueGetter: js.UndefOr[ValueGetter] = js.native
    //      var `type`: String | js.Array[String]
    var headerName: js.UndefOr[String] = js.native
    var filter: js.UndefOr[String | Boolean] = js.native
    var sortable: js.UndefOr[Boolean] = js.native
    var cellRenderer: js.UndefOr[JSCellRenderer] = js.native
    var cellRendererFramework: js.UndefOr[ReactCellRenderer] = js.native
//    var tooltipComponent: js.UndefOr[ReactComponentFn[js.Any]] = js.native
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

  /** Events */
  @js.native
  trait CellClickedEvent extends js.Object {
    val column: Column
    val value: js.Any
    val data: js.Any
    val rowIndex: js.UndefOr[Int]
    val `type`: String
  }

  @js.native
  trait CellContextMenuEvent extends js.Object {
    val column: Column
    val value: js.Any
    val data: js.Any
    val rowIndex: js.UndefOr[Int]
  }

  @js.native
  trait ContextMenuParams extends js.Object {

  }

  @js.native
  trait ContextMenuItems extends js.Object {

  }

  object Filters extends Enumeration {
    val default, text, number = Value
  }
}