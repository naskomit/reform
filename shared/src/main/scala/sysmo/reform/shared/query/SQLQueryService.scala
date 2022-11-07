package sysmo.reform.shared.query

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.data.RecordFieldCodec
import sysmo.reform.shared.table.{LocalTable, Table, TableService}
import sysmo.reform.shared.types.RecordFieldType

case class SQLQuery(q: String, args: Seq[Any], ftypes: Seq[Option[RecordFieldType]])

trait QueryService[_F[+_]] extends TableService[_F]


trait SQLQueryService[_F[+_]] extends QueryService[_F] {
  def generate_sql(q: Query): F[SQLQuery]
  def run_query(sql: SQLQuery): F[Table[F]]

  override def query_table(q: Query): F[Table[F]] = {
    for {
      sql <- generate_sql(q)
      table <- run_query(sql)
    } yield table
  }
}

