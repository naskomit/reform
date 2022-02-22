package sysmo.reform.data

import sysmo.reform.shared.query.Query

import scala.concurrent.Future

trait TableDatasource[U] {
  type RemoteBatch = Future[Seq[U]]
  def row_count : Future[Int]
  def run_query(q : Query): RemoteBatch
}
