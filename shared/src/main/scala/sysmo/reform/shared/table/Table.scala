package sysmo.reform.shared.table

import cats.MonadThrow
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.runtime.{FLocal, LocalRuntime}
import sysmo.reform.shared.types.RecordType
import sysmo.reform.shared.util.MonadicIterator


trait Table[F[+_]] {
  val mt: MonadThrow[F]
  def schema: RecordType
  def nrow: F[Int]
  def get(row_id: Int, col_id: Int): F[Value]
  def row(row_id: Int): F[Table.Row]
  def row_iter: MonadicIterator[F, Table.Row]
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

trait LocalTable extends Table[FLocal]

