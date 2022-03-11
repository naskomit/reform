package sysmo.reform.shared.data

import sysmo.reform.shared.data.table.Table
import sysmo.reform.shared.query.Query

trait TableApi {
  def query_table(q : Query): Table
}
