package sysmo.reform.data

import sysmo.reform.shared.data.table.Table
import sysmo.reform.shared.query.Query

import scala.concurrent.Future

trait TableDatasource {
  type RemoteBatch = Future[Table]
  def row_count : Future[Int]
  def query_table(q : Query): RemoteBatch
}
