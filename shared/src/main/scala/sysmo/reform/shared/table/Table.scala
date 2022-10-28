package sysmo.reform.shared.table

import cats.MonadThrow
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.util.containers.FLocal
import sysmo.reform.shared.table.Table.Schema
import sysmo.reform.shared.types.RecordType
import sysmo.reform.shared.util.MonadicIterator


trait Table[F[+_]] {
  implicit val mt: MonadThrow[F]
  def schema: RecordType
  def nrow: F[Int]
//  def get(row_id: Int, col_id: Int): F[Value]
//  def row(row_id: Int): F[Table.Row]
  def row_iter: MonadicIterator[F, Table.Row]
}

trait RandomAccessTable[F[+_]] {
  def get(row_id: Int, col_id: Int): F[Value]
  def row(row_id: Int): F[Table.Row]
}

object Table {
  type Schema = RecordType
  trait Row {
    def schema: Schema
    protected def _get(col: Int): Value
    def get(col: Int): Value =
      if ((col >= 0) && (col < schema.fields.size)) _get(col) else Value.empty
    def get(col: String): Value = schema.field_index(col) match {
      case Some(ind) => _get(ind)
      case None => Value.empty
    }

  }

}

trait LocalTable extends Table[FLocal] with RandomAccessTable[FLocal] {
  val mt: MonadThrow[FLocal] = MonadThrow[FLocal]
}

case class LocalRowBasedTable(_schema: Schema, rows: Seq[Table.Row]) extends LocalTable {
  override def get(row_id: Int, col_id: Int): FLocal[Value] = row(row_id).map(_.get(col_id))

  override def row(row_id: Int): FLocal[Table.Row] =
    if (row_id < rows.size)
      FLocal(rows(row_id))
    else
      FLocal.error(new NoSuchElementException(s"Row $row_id requested in a table with ${rows.size} rows"))

  override def schema: Schema = _schema
  override def nrow: FLocal[Int] = FLocal(rows.length)
  override def row_iter: MonadicIterator[FLocal, Table.Row] =
    MonadicIterator.from_iterator(rows.iterator)
}