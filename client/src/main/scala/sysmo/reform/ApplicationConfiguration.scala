package sysmo.reform

import sysmo.reform.shared.data.TableDatasource

trait ApplicationConfiguration {
  val table_source: TableDatasource
}
