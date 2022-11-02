package sysmo.reform.shared.query

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.data.RecordFieldCodec
import sysmo.reform.shared.table.{Table, TableService}
import sysmo.reform.shared.types.RecordFieldType

case class SQLQuery(q: String, args: Seq[Any], ftypes: Seq[Option[RecordFieldType]])



trait QueryService[_F[+_]] extends TableService[_F] {
  def generate_sql(q: Query): F[SQLQuery]
  def run_query(sql: SQLQuery): F[Table[F]]

  override def query_table(q: Query): F[Table[F]] = {
    generate_sql(q)
      .flatMap(sql => run_query(sql))
  }
}

