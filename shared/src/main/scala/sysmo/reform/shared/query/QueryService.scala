package sysmo.reform.shared.query

import cats.MonadThrow
import sysmo.reform.shared.types.RecordFieldType

case class SQLQuery(q: String, args: Seq[Any], ftypes: Seq[Option[RecordFieldType]])

trait QueryService[_F[+_]] {
  type F[+X] = _F[X]
  implicit val mt: MonadThrow[_F]
  def generate_sql(q: Query): F[SQLQuery]
}

