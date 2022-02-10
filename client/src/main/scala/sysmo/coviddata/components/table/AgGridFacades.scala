package sysmo.coviddata.components.table

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.{Children, JsComponent}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object AgGridFacades {
  @js.native
  trait GridOptions extends js.Object {
    var rowModelType: String = js.native
    var datasource: TableDatasource  = js.native
  }

  @js.native
  trait IGetRowsParams extends js.Object {
    // The first row index to get.
    val startRow: Int = js.native
    // The first row index to NOT get.
    val endRow: Int = js.native
    // Callback to call for the result when successful.
    val successCallback: js.Function2[js.Array[_], Int, _] = js.native
    // Callback to call when the request fails.
    val failCallback: js.Any = js.native
    // If doing server side sorting, contains the sort model
    val sortModel: js.Any = js.native
    // If doing server side filtering, contains the filter model
    val filterModel: js.Any = js.native
    // The context as provided on `gridOptions.context`
    val context: js.Any = js.native
  }

  @js.native
  trait TableDatasource extends js.Object {
//    var rowCount: js.Function0[Int] = js.native
    var getRows: js.Function1[IGetRowsParams, _] = js.native
  }

  object AgGridColumnNativeComponent {

    @JSImport("ag-grid-react", "AgGridColumn")
    @js.native
    object AgGridColumnNative extends js.Object

    @js.native
    trait Props extends js.Object {
      var field: String = js.native
//      var `type`: String | js.Array[String]
      var headerName : String = js.native
    }

    val component = JsComponent[Props, Children.None, Null](AgGridColumnNative)

    def apply(col : AgGridColumn) = {
      val props = (new js.Object).asInstanceOf[Props]
      props.field = col.field
      if (col.headerName != null) {
        props.headerName = col.headerName
      }
      component.withProps(props)()
    }
  }

  object AgGridNativeComponent {

    @JSImport("ag-grid-react", "AgGridReact")
    @js.native
    object AgGridReact extends js.Object

    @js.native
    trait Props extends js.Object {
      var rowData: js.Object = js.native
      var reactUi: Boolean = js.native
      var gridOptions: js.Object = js.native
    }

    val component = JsComponent[Props, Children.Varargs, Null](AgGridReact)

    //
    def apply(datasource: TableDatasource, columns : Seq[AgGridColumn]) = {
      val p = (new js.Object).asInstanceOf[Props]

      p.reactUi = true

      val grid_options = (new js.Object).asInstanceOf[GridOptions]
      grid_options.rowModelType = "infinite"
      grid_options.datasource = datasource
      p.gridOptions = grid_options

      val children : Seq[ChildArg] = columns.map(
        x => AgGridColumnNativeComponent(x))

      component.withProps(p).withChildren(children:_*)()
    }
  }

}