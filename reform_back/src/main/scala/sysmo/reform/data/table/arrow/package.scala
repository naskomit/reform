package sysmo.reform.data.table

package object arrow {
  def table_manager: ArrowTableManager = ArrowTableManager()
  val implicits = ArrowVector
}
