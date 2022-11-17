package sysmo.reform.react.table.aggrid

import cats.MonadThrow
import org.scalajs.dom
import sysmo.reform.shared.expr.LogicalAnd
import sysmo.reform.shared.{query, table => T}
import sysmo.reform.shared.query.{BasicQuery, FieldSort, Fields, QueryFilter, QueryRange, QuerySort, QuerySource}
import sysmo.reform.shared.table.{LocalTable, Table}
import sysmo.reform.shared.expr.{Expression => E}
import cats.syntax.all._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

class AgGridSourceAdaptor[F[+_]](ds: T.TableService[F], source: QuerySource, schema: T.Table.Schema) {
  implicit val mt: MonadThrow[F] = ds.mt
  private def process_filter(filter_model : AgGridFacades.FilterModel): Option[QueryFilter] = {
    val filter_seq = filter_model.toMap.map {case (k, v) =>
      (k, AgGridFacades.extract_filter(v, k))
    }.collect {case(k, Some(flt)) => (k, flt)
    }.values.toSeq
    if (filter_seq.nonEmpty) {
      Some(query.QueryFilter(LogicalAnd(filter_seq: _*)))
    } else
      None
  }

  private def process_sort(sort_model: AgGridFacades.SortModel): Option[QuerySort] = {
    if (sort_model.isEmpty)
      None
    else
      Some(query.QuerySort(
        sort_model.toSeq.map(sort_item => {
          FieldSort(E.field(sort_item.colId), sort_item.sort == "asc")
        })
      ))
  }

  //  val columns = Columns(schema.fields.map(field => E.ColumnRef(field.name)))

  val fields = Fields(schema.fields.map(field =>
    E.field(field.name, Some(field))
  ))

  val native : AgGridFacades.TableDatasource = {
    val ag_ds = (new js.Object).asInstanceOf[AgGridFacades.TableDatasource]

    ag_ds.getRows = params => {
      val filter = process_filter(params.filterModel)
      val sort = process_sort(params.sortModel)
      val range = QueryRange(params.startRow, params.endRow - params.startRow)
      val query = BasicQuery(
        source = source, projection = fields, filter = filter, sort = sort, range = Some(range)
      )

      val f_data: ds.F[LocalTable] = ds.query_table_local(query)
      f_data.map{(table: T.LocalTable) =>
        val data_proxy = AgGridSourceAdaptor.table_proxy(table)
        //          val data = table.row_iter.toJSArray
        val requested_rows = params.endRow - params.startRow
        val nrow = table.nrow.get
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

object AgGridSourceAdaptor{
  @js.native
  @JSGlobal
  class Proxy(target: Any, handler: js.Any) extends js.Object

  def table_proxy(table: LocalTable): Proxy = {
    val getter : js.Function3[LocalTable, String, js.Any, Option[Table.Row]] =
      (table: LocalTable, prop: String, receiver: js.Any) => {
        val index = prop.toInt
        if (index < table.nrow.get) {
          table.row(index).to_option
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
  def apply[F[+_]](ds: T.TableService[F], source: QuerySource, schema: T.Table.Schema): AgGridSourceAdaptor[F] =
    new AgGridSourceAdaptor(ds, source, schema)
}
