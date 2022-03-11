package sysmo.reform.shared.data

import sysmo.reform.shared.data.table.default_impl.{DefaultTableManager, DefaultVector}

import scala.util.Using

package object table {
  private var global_table_manager: Option[TableManager] = None

  private val dynamic_table_managers = new scala.util.DynamicVariable[Option[TableManager]](None)

  def enable_global_manager(flag: Boolean): Unit = {
    global_table_manager match {
      case Some(tm) if !flag => {
        tm.close()
        global_table_manager = None
      }
      case None if flag => {
        global_table_manager = Some(default.create_table_manager)
      }
      case _ => // Do nothing
    }
  }

  def with_table_manager[A](tm: TableManager = null)(f: TableManager => A): A = {
    Using(if (tm == null) default.create_table_manager else tm)(manager => {
      dynamic_table_managers.withValue(Some(manager))(
        f(manager)
      )
    }).get
  }

  def table_manager: TableManager =
    if (dynamic_table_managers.value.isEmpty)
      if (global_table_manager.isEmpty)
        throw new IllegalStateException("No active table manager! Use 'with_table_manager' and/or enable_global_manager(true)")
      else
        global_table_manager.get
    else
      dynamic_table_managers.value.get


  object default {
    def create_table_manager: DefaultTableManager = DefaultTableManager()
    val implicits = DefaultVector
  }
}
