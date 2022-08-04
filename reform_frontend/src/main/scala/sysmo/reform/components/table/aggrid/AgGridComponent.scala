package sysmo.reform.components.table.aggrid

import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.{expr => E}
import sysmo.reform.shared.{query => Q}
import sysmo.reform.components.table.aggrid.{AgGridFacades => agf}
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._

import scala.scalajs.js
import scala.util.{Failure, Success}
import sysmo.reform.shared.table2.{TableService, Table, SelectionHandler}

class AgGridSourceAgaptor[U[_]](ds: TableService[U], source: Q.QuerySource, schema: Table.Schema) {
  private def process_filter(filter_model : AgGridFacades.FilterModel): Option[Q.QueryFilter] = {
    val filter_seq = filter_model.toMap.map {case (k, v) =>
      (k, AgGridFacades.extract_filter(v, k))
    }.collect {case(k, Some(flt)) => (k, flt)
    }.values.toSeq
    if (filter_seq.nonEmpty) {
      Some(Q.QueryFilter(E.LogicalAnd(filter_seq: _*)))
    } else
      None
  }

  private def process_sort(sort_model: AgGridFacades.SortModel): Option[Q.QuerySort] = {
    if (sort_model.isEmpty)
      None
    else
      Some(Q.QuerySort(
        sort_model.toSeq.map(sort_item => {
          Q.ColumnSort(E.ColumnRef(sort_item.colId), sort_item.sort == "asc")
        }): _*
      ))
  }

//  var total_rows : Int = -1
//  ds.row_count.foreach(x => total_rows = x)

  val columns = schema.fields.map(field => E.ColumnRef(field.name))

  val native : AgGridFacades.TableDatasource = {
    val ag_ds = (new js.Object).asInstanceOf[AgGridFacades.TableDatasource]
    //    ds.rowCount = () => total_rows
    ag_ds.getRows = params => {
      dom.console.log(params)
      val filter = process_filter(params.filterModel)
      var sort = process_sort(params.sortModel)
      var range = Q.QueryRange(params.startRow, params.endRow - params.startRow)
      val query = Q.BasicQuery(
        source = source, columns = Some(columns), filter = filter, sort = sort, range = Some(range)
      )

      val f_data: ds.MT[Table] = ds.query_table(query)
      ds.mt.map(f_data){(table: Table) =>
        val data_proxy = agf.table_proxy(table)
        //          val data = table.row_iter.toJSArray
        val requested_rows = params.endRow - params.startRow
        val total_rows = if (table.nrow < requested_rows)
          params.startRow + table.nrow
        else
          -1
        params.successCallback(data_proxy, total_rows)

      }

      ds.mt.handleError(f_data){error =>
        params.failCallback()
        throw new RuntimeException("Failed fetching table data", error)
      }

//      ds.().onComplete {
//        case Success(table) => {
//          val data_proxy = agf.table_proxy(table)
////          val data = table.row_iter.toJSArray
//          val requested_rows = params.endRow - params.startRow
//          val total_rows = if (table.nrow < requested_rows)
//            params.startRow + table.nrow
//          else
//            -1
//          params.successCallback(data_proxy, total_rows)
//        }
//        case Failure(e) => {
//          params.failCallback()
//          throw new RuntimeException("Failed fetching table data", e)
//        }
//      }
    }
    ag_ds
  }
//  val native = toNative()

}

object AgGridSourceAgaptor{
  def apply[U[_]](ds: TableService[U], source: Q.QuerySource, schema: Table.Schema): AgGridSourceAgaptor[U] =
    new AgGridSourceAgaptor(ds, source, schema)
}

object AgGridComponent extends ReactComponent {
  import japgolly.scalajs.react.ScalaComponent
  import japgolly.scalajs.react.component.Scala.BackendScope
  trait Props {
    type U[_]
    val ds : AgGridSourceAgaptor[U]
    val columns : Seq[agf.ColumnProps]
    val height: String
    val selection_handler: Option[SelectionHandler]
  }

  case class State()

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      <.div(
        ^.cls := "ag-theme-alpine",
        ^.height := p.height,
        ^.width := "100%",
        agf.AgGridNativeComponent(p.ds.native, p.columns, p.selection_handler)
      )
    }
  }

  val component = ScalaComponent.builder[Props]("AgGrid")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply[T[_]](_ds: TableService[T], _table: Q.QuerySource, _schema: Table.Schema,
                  _columns: Seq[agf.ColumnProps], _height: String, _selection_handler: Option[SelectionHandler]): Unmounted =
    component(new Props {
      type U[X] = T[X]
      val ds = AgGridSourceAgaptor(_ds, _table, _schema)
      val columns = _columns
      val height = _height
      val selection_handler = _selection_handler
    })
}
