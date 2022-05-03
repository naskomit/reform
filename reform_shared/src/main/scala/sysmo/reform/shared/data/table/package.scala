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

  def table_from_columns(table_manager: TableManager, schema: Schema, data: Tuple2[String, Seq[_]]*): Table = {
    assert(schema.fields.size == data.length, "Table data must be ")
    val nrow = data(0)._2.length
    for (i <- 0 until data.length) {
      if (data(i)._2.length != nrow) {
        throw new IllegalArgumentException(s"Number of data points in column ${data(i)._1} is different from the number of rows $nrow" )
      }
    }
    val tb_1 = table_manager.incremental_table_builder(schema)
    for (r <- 0 until nrow) {
      val row_data = data.map {
        case (name, col_data) => (name, Some(col_data(r)))
      }.toMap
      tb_1 :+ row_data
    }
    tb_1.toTable
  }



  object default {
    def create_table_manager: DefaultTableManager = DefaultTableManager()
    val implicits = DefaultVector
  }

  implicit class ops(table: Table) extends TableOps(table)
}
