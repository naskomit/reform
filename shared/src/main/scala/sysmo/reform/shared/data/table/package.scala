package sysmo.reform.shared.data

import sysmo.reform.shared.data.table.default_impl.{DefaultTableManager, DefaultVector}

package object table {
  trait PrettyPrinter[V] {
    def pprint(v: V): String
  }

  val table_managers = new scala.util.DynamicVariable[TableManager](null)
  def with_table_manager(tm: TableManager = null)(f: TableManager => Unit): Unit = {
    val manager = if (tm == null) default.create_table_manager else tm
    table_managers.withValue(manager)(f(manager))
    manager.close()
  }

  def table_manager: TableManager =
    if (table_managers.value == null)
      throw new IllegalStateException("No active table manager! Use 'with_table_manager'")
    else
      table_managers.value

  def pprint[V : PrettyPrinter](v: V)(implicit pp: PrettyPrinter[V]): String = {
    pp.pprint(v)
  }

  object default {
    def create_table_manager: DefaultTableManager = DefaultTableManager()
    val implicits = DefaultVector
  }
}
