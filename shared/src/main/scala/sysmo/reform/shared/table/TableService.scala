package sysmo.reform.shared.table

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.query.Query
import sysmo.reform.shared.types.RecordType

trait TableService[_F[+_]] {
  type F[+X] = _F[X]
  implicit val mt: MonadThrow[F]
  def list_tables(): F[Seq[Table.Schema]]
  def table_schema(table_id: String): F[Table.Schema]
  def query_table(q : Query): F[Table[F]]
  def query_table_local(q : Query): F[LocalTable] =
    query_table(q).flatMap(_.cache)
}
