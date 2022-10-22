package sysmo.reform.shared.table

import cats.MonadThrow
import sysmo.reform.shared.types.RecordType

trait TableService[_F[+_]] {
  type F[+X] = _F[X]
  implicit val mt: MonadThrow[F]
  def list_tables(): F[Seq[Table.Schema]]
  def table_schema(table_id: String): F[Table.Schema]
  def query_table(q : Query): F[Table[F]]
  def cache_locally(table: Table[F]): F[LocalTable] = {
    table.row_iter.traverse(rows =>
      LocalRowBasedTable(table.schema, rows)
    )
  }
}
