package sysmo.reform

import sysmo.reform.shared.data.TableService

trait ApplicationConfiguration {
  val table_source: TableService
}
