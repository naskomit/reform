package sysmo.reform.components.table

import sysmo.reform.shared.data.{table => sdt}


trait TableSelectionHandler {
//  val id_columns: Seq[String]
  val mode: TableSelectionHandler.RowSelectionMode =
    TableSelectionHandler.NoSelection
  def on_change(selection: Iterable[sdt.Row])
}

object TableSelectionHandler {
  sealed trait RowSelectionMode
  case object NoSelection extends RowSelectionMode
  case object SingleRow extends RowSelectionMode
  case object MultiRow extends RowSelectionMode
}