package sysmo.reform.shared.data

import sysmo.reform.shared.data.table.default_impl.{DefaultTableManager, DefaultVector}

package object table {
  object default {
    def table_manager: DefaultTableManager = DefaultTableManager()
    val implicits = DefaultVector
  }
}
