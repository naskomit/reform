package sysmo.reform.shared.data.table

class Row(table: Table, row_data: Seq[Value[_]]) {
  def schema: Schema = table.schema
  def get(col: Int): Value[_] = if (col < row_data.length) row_data(col) else Value.empty
  def get(col: String): Value[_] = schema.field_index(col) match {
    case Some(ind) => row_data(ind)
    case None => Value.empty
  }

  override def toString: String = row_data.toString
}

class RowIterator(table: Table, var current_row: Int = 0) extends Iterator[Row] {
  override def hasNext: Boolean = current_row < table.nrow

  override def next(): Row = {
    val row = table.row(current_row)
    current_row += 1
    row
  }
}

