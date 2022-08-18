package sysmo.reform.shared.table

trait SelectionHandler {
  val mode: SelectionHandler.RowSelectionMode =
    SelectionHandler.NoSelection
  def on_change(selection: Iterable[Table.Row])
}

object SelectionHandler {
  sealed trait RowSelectionMode
  case object NoSelection extends RowSelectionMode
  case object SingleRow extends RowSelectionMode
  case object MultiRow extends RowSelectionMode
}