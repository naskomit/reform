package sysmo.reform.shared.data.table

import scala.collection.mutable

class TableOps(table: Table) {

  def pprint(): Unit  = {
    import sysmo.reform.shared.util.{pprint => pp}
    import sysmo.reform.shared.data.table.Printers._
    pp.pprint(table)
  }

  def group_by[A](key_fn: Row => A): Map[A, Table] = {
    val builder_map = mutable.Map[A, TableBuilder]()
    val table_manager = table.manager
    for (row <- table.row_iter) {
      val key = key_fn(row)
      val builder = builder_map.getOrElseUpdate(key, table_manager.incremental_table_builder(table.schema))
      builder.append_record(row)
    }
    builder_map.map {
      case (k, v) => (k, v.toTable)
    }.toMap
  }
}
