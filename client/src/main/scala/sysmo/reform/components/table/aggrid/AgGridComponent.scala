package sysmo.reform.components.table.aggrid

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.shared.query._
import sysmo.reform.components.table.aggrid.{AgGridFacades => agf}
import sysmo.reform.data.TableDatasource

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._



class AgGridSourceAgaptor[U](ds: TableDatasource[U], source: QuerySource) {
  private def process_filter(filter_model : AgGridFacades.FilterModel): Option[QueryFilter] = {
    val filter_seq = filter_model.toMap.map {case (k, v) =>
      val extractor = new AgGridFacades.FilterModelJSExtractor(k)
      v match {
        case extractor(x) => (k, x)
      }
    }.values.toSeq
    if (filter_seq.nonEmpty) {
      Some(QueryFilter(LogicalAnd(filter_seq)))
    } else
      None
  }

  private def process_sort(sort_model: AgGridFacades.SortModel): Option[QuerySort] = {
    None
  }

  var total_rows : Int = -1
  ds.row_count.foreach(x => total_rows = x)

  val native : AgGridFacades.TableDatasource = {
    val ag_ds = (new js.Object).asInstanceOf[AgGridFacades.TableDatasource]
    //    ds.rowCount = () => total_rows
    ag_ds.getRows = params => {
      val filter = process_filter(params.filterModel)
      var sort = process_sort(params.sortModel)
      var range = None
      val query = BasicQuery(
        source, filter, sort, range
      )
      ds.run_query(query).foreach(
        data => params.successCallback(data.toJSArray, total_rows)
      )
    }
    ag_ds
  }
//  val native = toNative()

}

object AgGridSourceAgaptor {
  def apply[U](ds: TableDatasource[U], source: QuerySource): AgGridSourceAgaptor[U] = new AgGridSourceAgaptor(ds, source)
}

object AgGridComponent {
  import japgolly.scalajs.react.ScalaComponent
  import japgolly.scalajs.react.component.Scala.BackendScope
  case class Props(ds : AgGridSourceAgaptor[_], columns : Seq[AgGridColumn])
  case class State()

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.className:="row row-condensed",
          <.div(^.className:="page-subtitle",
            <.div(<.h2("AGGrid")),
          )
        ),
        <.div(^.className:="row",
          <.div(
            ^.cls := "ag-theme-alpine",
            ^.height := "800px",
            ^.width := "100%",
            agf.AgGridNativeComponent(p.ds.native, p.columns)
          )
        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("AgGrid")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(ds : TableDatasource[_], table: QuerySource, columns : Seq[AgGridColumn]) =
    component(Props(AgGridSourceAgaptor(ds, table), columns))

}
