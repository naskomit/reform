package sysmo.reform.react.table.aggrid

import cats.MonadThrow
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import sysmo.reform.shared.{table => T}
import sysmo.reform.shared.{expr => E}
import cats.implicits._
import sysmo.reform.react.ReactComponent
import sysmo.reform.shared.table.{LocalTable, SelectionHandler}

import scala.scalajs.js

class AgGridSourceAgaptor[F[+_]](ds: T.TableService[F], source: T.QuerySource, schema: T.Table.Schema) {
  implicit val mt: MonadThrow[F] = ds.mt
  private def process_filter(filter_model : AgGridFacades.FilterModel): Option[T.QueryFilter] = {
    val filter_seq = filter_model.toMap.map {case (k, v) =>
      (k, AgGridFacades.extract_filter(v, k))
    }.collect {case(k, Some(flt)) => (k, flt)
    }.values.toSeq
    if (filter_seq.nonEmpty) {
      Some(T.QueryFilter(E.LogicalAnd(filter_seq: _*)))
    } else
      None
  }

  private def process_sort(sort_model: AgGridFacades.SortModel): Option[T.QuerySort] = {
    if (sort_model.isEmpty)
      None
    else
      Some(T.QuerySort(
        sort_model.toSeq.map(sort_item => {
          T.ColumnSort(E.ColumnRef(sort_item.colId), sort_item.sort == "asc")
        }): _*
      ))
  }

  val columns = schema.fields.map(field => E.ColumnRef(field.name))

  val native : AgGridFacades.TableDatasource = {
    val ag_ds = (new js.Object).asInstanceOf[AgGridFacades.TableDatasource]

    ag_ds.getRows = params => {
      dom.console.log(params)
      val filter = process_filter(params.filterModel)
      val sort = process_sort(params.sortModel)
      val range = T.QueryRange(params.startRow, params.endRow - params.startRow)
      val query = T.BasicQuery(
        source = source, columns = Some(columns), filter = filter, sort = sort, range = Some(range)
      )

      val f_data: ds.F[LocalTable] = ds.query_table(query).flatMap(ds.cache_locally)
      f_data.map{(table: T.LocalTable) =>
        val data_proxy = AgGridFacades.table_proxy(table)
        //          val data = table.row_iter.toJSArray
        val requested_rows = params.endRow - params.startRow
        val nrow = table.nrow.toOption.get
        val total_rows = if ( nrow < requested_rows)
          params.startRow + nrow
        else
          -1
        params.successCallback(data_proxy, total_rows)

      }

      ds.mt.handleError(f_data){error =>
        params.failCallback()
        throw new RuntimeException("Failed fetching table data", error)
      }
    }
    ag_ds
  }
}

object AgGridSourceAgaptor{
  def apply[F[+_]](ds: T.TableService[F], source: T.QuerySource, schema: T.Table.Schema): AgGridSourceAgaptor[F] =
    new AgGridSourceAgaptor(ds, source, schema)
}

object AgGridComponent extends ReactComponent {
  import japgolly.scalajs.react.ScalaComponent
  import japgolly.scalajs.react.component.Scala.BackendScope
  trait Props {
    type F[+_]
    val ds : AgGridSourceAgaptor[F]
    val columns : Seq[AgGridFacades.ColumnProps]
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
        AgGridFacades.AgGridNativeComponent(p.ds.native, p.columns, p.selection_handler)
      )
    }
  }

  val component = ScalaComponent.builder[Props]("AgGrid")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply[_F[+_]](_ds: T.TableService[_F], _table: T.QuerySource, _schema: T.Table.Schema,
                    _columns: Seq[AgGridFacades.ColumnProps], _height: String, _selection_handler: Option[SelectionHandler]): Unmounted =
    component(new Props {
      override type F[+X] = _F[X]
      val ds = AgGridSourceAgaptor(_ds, _table, _schema)
      val columns = _columns
      val height = _height
      val selection_handler = _selection_handler
    })
}
