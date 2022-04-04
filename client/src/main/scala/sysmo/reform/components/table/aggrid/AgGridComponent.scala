package sysmo.reform.components.table.aggrid

import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.query._
import sysmo.reform.components.table.aggrid.{AgGridFacades => agf}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}
import sysmo.reform.shared.data.{TableService, table => sdt}
import sysmo.reform.shared.{query => Q}

class AgGridSourceAgaptor(ds: TableService, source: QuerySource, schema: sdt.Schema) {
  private def process_filter(filter_model : AgGridFacades.FilterModel): Option[QueryFilter] = {
    val filter_seq = filter_model.toMap.map {case (k, v) =>
      (k, AgGridFacades.extract_filter(v, k))
    }.collect {case(k, Some(flt)) => (k, flt)
    }.values.toSeq
    if (filter_seq.nonEmpty) {
      Some(QueryFilter(LogicalAnd(filter_seq: _*)))
    } else
      None
  }

  private def process_sort(sort_model: AgGridFacades.SortModel): Option[QuerySort] = {
    if (sort_model.isEmpty)
      None
    else
      Some(QuerySort(
        sort_model.toSeq.map(sort_item => {
          ColumnSort(ColumnRef(sort_item.colId), sort_item.sort == "asc")
        }): _*
      ))
  }

//  var total_rows : Int = -1
//  ds.row_count.foreach(x => total_rows = x)

  val columns = schema.fields.map(field => Q.ColumnRef(field.name))

  val native : AgGridFacades.TableDatasource = {
    val ag_ds = (new js.Object).asInstanceOf[AgGridFacades.TableDatasource]
    //    ds.rowCount = () => total_rows
    ag_ds.getRows = params => {
      dom.console.log(params)
      val filter = process_filter(params.filterModel)
      var sort = process_sort(params.sortModel)
      var range = QueryRange(params.startRow, params.endRow - params.startRow)
      val query = BasicQuery(
        source = source, columns = Some(columns), filter = filter, sort = sort, range = Some(range)
      )

      ds.query_table(query).onComplete {
        case Success(table) => {
          val data_proxy = agf.table_proxy(table)
//          val data = table.row_iter.toJSArray
          val requested_rows = params.endRow - params.startRow
          val total_rows = if (table.nrow < requested_rows)
            params.startRow + table.nrow
          else
            -1
          params.successCallback(data_proxy, total_rows)
        }
        case Failure(e) => {
          params.failCallback()
          throw new RuntimeException("Failed fetching table data", e)
        }
      }
    }
    ag_ds
  }
//  val native = toNative()

}

object AgGridSourceAgaptor{
  def apply(ds: TableService, source: QuerySource, schema: sdt.Schema): AgGridSourceAgaptor =
    new AgGridSourceAgaptor(ds, source, schema)
}

object AgGridComponent extends ReactComponent {
  import japgolly.scalajs.react.ScalaComponent
  import japgolly.scalajs.react.component.Scala.BackendScope
  case class Props(ds : AgGridSourceAgaptor, columns : Seq[agf.ColumnProps], height: String)
  case class State()

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      <.div(
        ^.cls := "ag-theme-alpine",
        ^.height := p.height,
        ^.width := "100%",
        agf.AgGridNativeComponent(p.ds.native, p.columns)
      )
    }
  }

  val component = ScalaComponent.builder[Props]("AgGrid")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(ds: TableService, table: QuerySource, schema: sdt.Schema, columns: Seq[agf.ColumnProps], height: String): Unmounted =
    component(Props(AgGridSourceAgaptor(ds, table, schema), columns, height))

}
