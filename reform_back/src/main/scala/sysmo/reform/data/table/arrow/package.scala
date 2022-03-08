package sysmo.reform.data.table

package object arrow {
  def create_table_manager: ArrowTableManager = ArrowTableManager()
  val implicits = ArrowVector
}
