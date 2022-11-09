package sysmo.reform.shared.table

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.containers.FLocal
import sysmo.reform.shared.containers.implicits._
import sysmo.reform.shared.table.Table.Schema
import sysmo.reform.shared.types.RecordType
import sysmo.reform.shared.util.MonadicIterator


trait Table[F[+_]] {
  implicit val mt: MonadThrow[F]
  def schema: RecordType
  def nrow: F[Int]
  def ncol: Int = schema.fields.size
  def row_iter: MonadicIterator[F, Table.Row]
  def cache: F[LocalTable] = row_iter.traverse(rows =>
    LocalRowBasedTable(schema, rows)
  )
}

trait RandomAccessTable[F[+_]] {
  def get(row_id: Int, col_id: Int): F[Value]
  def row(row_id: Int): F[Table.Row]
}

object Table {
  type Schema = RecordType
  trait Row {
    def schema: Schema
    def ncol: Int = schema.fields.size
    protected def _get(col: Int): Value
    def get(col: Int): Value =
      if ((col >= 0) && (col < ncol)) _get(col) else Value.empty
    def get(col: String): Value = schema.field_index(col) match {
      case Some(ind) => _get(ind)
      case None => Value.empty
    }

  }

  object Row {
    case class SeqRow(val schema: Schema, values: Seq[Value]) extends Row {
      override protected def _get(col: Int): Value = values(col)
    }
  }

}

trait LocalTable extends Table[FLocal]
  with RandomAccessTable[FLocal]
  with Iterable [Table.Row] {
  val mt: MonadThrow[FLocal] = MonadThrow[FLocal]
  override def cache: FLocal[LocalTable] = FLocal(this)
}

case class LocalRowBasedTable(schema: Schema, rows: Seq[Table.Row]) extends LocalTable {
  override def get(row_id: Int, col_id: Int): FLocal[Value] = row(row_id).map(_.get(col_id))

  override def row(row_id: Int): FLocal[Table.Row] =
    if (row_id < rows.size)
      FLocal(rows(row_id))
    else
      FLocal.error(new NoSuchElementException(s"Row $row_id requested in a table with ${rows.size} rows"))

  override def nrow: FLocal[Int] = FLocal(rows.length)
  override def row_iter: MonadicIterator[FLocal, Table.Row] =
    MonadicIterator.from_iterator(rows.iterator)

  override def iterator: Iterator[Table.Row] = new Iterator[Table.Row] {
    var row_number = 0
    override def hasNext: Boolean = row_number < rows.size
    override def next(): Table.Row = {
      val item = rows(row_number)
      row_number += 1
      item
    }
  }
}