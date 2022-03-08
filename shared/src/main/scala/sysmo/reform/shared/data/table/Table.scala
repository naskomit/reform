package sysmo.reform.shared.data.table

trait TableBuilder {
  def :+(row_data: Map[String, Option[Any]]): Unit
  def toTable: Table
}

class IncrementalTableBuilder(schema: Schema, col_builders: Seq[SeriesBuilder]) extends TableBuilder {
  private val column_map = col_builders.zip(schema.fields).map(x => (x._2.name, x._1)).toMap
  override def :+(row_data: Map[String, Option[Any]]): Unit = {
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
  def column(name: String): Series
  def row(row_id: Int): Row
  def row_iter: Iterator[Row]
  def column_iter: Iterator[Series] = new Iterator[Series] {
    var index = 0
    override def hasNext: Boolean = index < ncol
    override def next(): Series = {
      index += 1
      column(index - 1)
    }
  }
}

class TableImpl(var schema: Schema, var column_data: Seq[Series]) extends Table {
  private var columnMap: Map[String, Series] =
    column_data.zip(schema.fields).map(x => (x._2.name, x._1)).toMap

  def nrow: Int = if (column_data.isEmpty) 0 else column_data(0).length
  def ncol: Int = column_data.size
  def get(row: Int, col: Int): Value = column_data(col).get(row)
  def column(index: Int): Series = column_data(index)
  def column(name: String): Series = columnMap(name)
  def row(row_id: Int): Row = {
    new Row(this, column_data.map(cd => cd.get(row_id)))
  }
  def row_iter: Iterator[Row] = new RowIterator(this)
}