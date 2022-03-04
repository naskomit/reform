package sysmo.reform.shared.data.table

trait TableBuilder {
  def :+(row_data: Map[String, _]): Unit
  def toTable: Table
}

class IncrementalTableBuilder(schema: Schema, col_builders: Seq[SeriesBuilder]) extends TableBuilder {
  private val column_map = col_builders.zip(schema.fields).map(x => (x._2.name, x._1)).toMap
  override def :+(row_data: Map[String, _]): Unit = {
    for (field <- schema.fields) {
      column_map(field.name) :+ row_data(field.name)
    }
  }

  override def toTable: Table = new TableImpl(schema, col_builders.map(_.toSeries))
}

trait Table {
  var schema: Schema
  def nrow: Int
  def ncol: Int
  def get(row: Int, col: Int): Value
  def column(index: Int): Series
  def row(row_id: Int): Row

  def pprint: String
}

class TableImpl(var schema: Schema, var column_data: Seq[Series]) extends Table {
  private var columnMap: Map[String, Series] =
    column_data.zip(schema.fields).map(x => (x._2.name, x._1)).toMap

  def nrow: Int = if (column_data.isEmpty) 0 else column_data(0).length
  def ncol: Int = column_data.size
  def get(row: Int, col: Int): Value = column_data(col).get(row)
  def column(index: Int): Series = column_data(index)
  def row(row_id: Int): Row = {
    new Row(this, column_data.map(cd => cd.get(row_id)))
  }

  def row_iter: Iterator[Row] = new RowIterator(this)

  def pprint: String = {
    val max_field_width = 10
    val v_delim = "="
    val h_delim = "|"
    val sb = new StringBuilder("")
    val padding = 3
    val field_width = schema.fields.map(f => f.name.length + 2 * padding)
    val vdelim = v_delim * (field_width.sum + schema.fields.length + 1)
    // top line
    sb ++= vdelim
    sb ++= "\n"
    // col names
    sb ++= h_delim
    schema.fields.foreach(f => {
      sb ++= " " * padding
      sb ++= f.name
      sb ++= " " * padding
      sb ++= h_delim
    })
    sb ++= "\n"
    // header line
    sb ++= vdelim
    sb ++= "\n"
    for (row <- row_iter) {
      sb ++= h_delim
      for (col <- 0 until schema.fields.size) {
        val value_rep = row.get(col).as_char.get
        val extra_padding_size = field_width(col) - value_rep.length - padding
        sb ++= " " * padding
        sb ++= value_rep + (" " * extra_padding_size)
        sb ++= h_delim
      }
      sb ++= "\n"
      sb ++= vdelim
      sb ++= "\n"
    }
    sb.toString()
  }
}