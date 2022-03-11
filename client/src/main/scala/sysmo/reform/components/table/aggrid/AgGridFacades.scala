package sysmo.reform.components.table.aggrid

import org.scalajs.dom
import sysmo.reform.shared.query._

import scala.scalajs.js
import js.JSConverters._
import scala.scalajs.js.annotation.JSImport
import japgolly.scalajs.react.{Children, JsComponent}

import scala.scalajs.js.|
import scalajs.js.annotation.JSGlobal
import sysmo.reform.shared.data.{table => sdt}

object AgGridFacades {
  @js.native
  trait GridOptions extends js.Object {
    var rowModelType: String = js.native
    var datasource: TableDatasource  = js.native
  }

  type FilterModel = js.Dictionary[ColumnFilter]
  type SortModel = js.Array[SortModelItem]

  @js.native
  trait ColumnFilter extends js.Object {
    val filterType: String = js.native
  }

  class FilterModelJSExtractor(column: String) {
    def unapply(flt : ColumnFilter): Option[PredicateExpression] = {
      if (flt.hasOwnProperty("filterType")) {
        flt.filterType match {
          case "text" => {
            val f = flt.asInstanceOf[TextFilterModelJS]
            val predicate = f.`type` match {
              case "equals" => StringPredicateOp.Equal
              case "notEqual" => StringPredicateOp.NotEqual
              case "contains" => StringPredicateOp.Containing
              case "notContains" => StringPredicateOp.NotContaining
              case "startsWith" => StringPredicateOp.StartingWith
              case "endsWith" => StringPredicateOp.EndingWith

            }
            Some(StringPredicate(predicate, ColumnRef(column), Val(f.filter)))
          }

          case "number" => {
            val f = flt.asInstanceOf[NumberFilterModelJS]
            if (f.`type` == "inRange") {
              Some(LogicalAnd(
                NumericalPredicate(NumericalPredicateOp.>=, ColumnRef(column), Val(f.filter)),
                NumericalPredicate(NumericalPredicateOp.<=, ColumnRef(column), Val(f.filterTo))
              ))
            } else {
              val predicate = f.`type` match {
                case "equals" => NumericalPredicateOp.Equal
                case "notEqual" => NumericalPredicateOp.NotEqual
                case "lessThan" => NumericalPredicateOp.<
                case "lessThanOrEqual" => NumericalPredicateOp.<=
                case "greaterThan" => NumericalPredicateOp.>
                case "greaterThanOrEqual" => NumericalPredicateOp.>=

              }
              Some(NumericalPredicate(predicate, ColumnRef(column), Val(f.filter)))
            }
          }

          case x => {
            dom.console.warn(f"Unimplemented filter type $x")
            None
          }
        }
      } else
        None
    }
  }

  @js.native
  trait TextFilterModelJS extends ColumnFilter {
    val filter: String = js.native
    val filterTo: js.UndefOr[String] = js.native
    val `type`: String = js.native
  }

  @js.native
  trait NumberFilterModelJS extends ColumnFilter {
    val filter: Double = js.native
    val filterTo: Double = js.native
    val `type`: String = js.native
  }

  @js.native
  trait DateFilterModelJS extends ColumnFilter {
    val dateFrom: String = js.native
    val dateTo: String = js.native
    val `type`: String = js.native
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

    }

    val component = JsComponent[Props, Children.None, Null](AgGridReact)

    def apply(datasource: TableDatasource, columns : Seq[ColumnProps]) = {
      val p = (new js.Object).asInstanceOf[Props]

      p.reactUi = true

      val grid_options = (new js.Object).asInstanceOf[GridOptions]
      grid_options.rowModelType = "infinite"
      grid_options.datasource = datasource
      p.gridOptions = grid_options
      p.columnDefs = columns.toJSArray

      component.withProps(p)()
    }
  }
}
