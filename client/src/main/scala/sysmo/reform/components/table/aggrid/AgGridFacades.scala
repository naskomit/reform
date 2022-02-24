package sysmo.reform.components.table.aggrid

import org.scalajs.dom
import sysmo.reform.shared.query._

import scala.scalajs.js
import js.JSConverters._
import scala.scalajs.js.annotation.JSImport
import japgolly.scalajs.react.{Children, JsComponent}

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
      dom.console.log(flt)
      if (flt.hasOwnProperty("filterType")) {
        if (flt.filterType == "text") {
          println("Text filter")
          val f = flt.asInstanceOf[TextFilterModelJS]
          Some(NumericalPredicate(NumericalPredicateOp.Equal, StringValue(f.filter), ColumnRef(column)))
        }
//        else if (flt.filterType == "number") {
//          val f = flt.asInstanceOf[NumberFilterModelJS]
//          Some(NumberFilter(f.filter, f.filterTo, f.`type`))
//        } else if (flt.filterType == "date") {
//          val f = flt.asInstanceOf[DateFilterModelJS]
//          Some(DateFilter(f.dateFrom, f.dateTo, f.`type`))
//        }
        else None
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
    val successCallback: js.Function2[js.Array[_], Int, _] = js.native
    /** Callback to call when the request fails. */
    val failCallback: js.Any = js.native
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



  object AgGridNativeComponent {

    @JSImport("ag-grid-react", "AgGridReact")
    @js.native
    object AgGridReact extends js.Object

    @js.native
    trait ColDef extends js.Object {
      var field: String = js.native
      //      var `type`: String | js.Array[String]
      var headerName: String = js.native
      var filter: String = js.native
    }

    @js.native
    trait Props extends js.Object {
      var columnDefs: js.Array[ColDef] = js.native
      var rowData: js.Object = js.native
      var reactUi: Boolean = js.native
      var gridOptions: GridOptions = js.native

    }

    val component = JsComponent[Props, Children.None, Null](AgGridReact)

    //
    def apply(datasource: TableDatasource, columns : Seq[AgGridColumn]) = {
      val p = (new js.Object).asInstanceOf[Props]

      p.reactUi = true

      val grid_options = (new js.Object).asInstanceOf[GridOptions]
      grid_options.rowModelType = "infinite"
      grid_options.datasource = datasource
      p.gridOptions = grid_options
      p.columnDefs = columns.map(x => {
        val cdef = (new js.Object).asInstanceOf[ColDef]

        cdef.field = x.field
        cdef.headerName = x.headerName
        cdef.filter = "agTextColumnFilter"

        cdef
      }).toJSArray

      component.withProps(p)()
    }
  }
}
