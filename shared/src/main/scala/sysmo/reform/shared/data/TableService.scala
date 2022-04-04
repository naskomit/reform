package sysmo.reform.shared.data

import sysmo.reform.shared.data.table.{Schema, Table}
import sysmo.reform.shared.query.Query
import sysmo.reform.shared.util.Named

import scala.concurrent.Future

trait TableService {
  type RemoteBatch = Future[Table]
//  def row_count : Future[Int]
  def list_tables(optionFilter: OptionFilter): Future[Seq[Named]]
  def table_schema(table_id: String): Future[Schema]
  def query_table(q : Query): RemoteBatch
}
