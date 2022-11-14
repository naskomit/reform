package sysmo.reform.storage.orientdb

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.logging.Logging
import sysmo.reform.shared.query.{Query, SQLGenerator, SQLTextualQuery, SQLQueryService}
import sysmo.reform.shared.table.Table.Schema
import sysmo.reform.shared.table.Table
import sysmo.reform.shared.types.{RecordType}
import sysmo.reform.shared.util.MonadicIterator

import scala.jdk.CollectionConverters._

class OrientDBSQLGenerator[F[+_]](implicit val mt: MonadThrow[F]) extends SQLGenerator[F] {

}

class OrientDBQueryService[_F[+_]](session: SessionImpl[_F])(
  implicit val mt: MonadThrow[_F]) extends SQLQueryService[_F] with Logging {
  lazy val sql_generator = new OrientDBSQLGenerator()

  override def list_tables(): F[Seq[Schema]] = mt.pure(Seq())

  // TODO
  override def table_schema(table_id: String): F[Schema] = ???

  override def generate_sql(q: Query): F[SQLTextualQuery] =
    sql_generator.generate(q)

  override def run_query(sql: SQLTextualQuery): F[Table[F]] = {
    logger.info(sql.q)
    for {
      result_schema <- {
        sql.ftypes.zipWithIndex.traverse {
          case (Some(ftype), i: Int) => mt.pure(ftype)
          case (None, i: Int) => mt.raiseError(new IllegalArgumentException(
            s"No field type provided for query field #${i}"
          ))
        }.map {ftypes =>
          val rec_type = RecordType("QuerySchema")
          ftypes.foreach(ftype => rec_type + ftype)
          rec_type: RecordType
        }
      }

      table <- {
        val _mt = mt
        val result_set: Iterator[F[Table.Row]]  = session
          .db_session.query(sql.q, sql.args:_*).asScala
          .map { row =>
            result_schema.fields.traverse { ftype =>
              session.rec_field_codec.read_value(ftype, row.toElement)
            }.map(row_values =>
              new Table.Row {
                override def schema: Table.Schema = result_schema
                override protected def _get(col: Int): Value = row_values(col)
              }
            )
          }

        mt.pure(new Table[F] {
          override implicit val mt: MonadThrow[F] = _mt
          override def schema: RecordType = result_schema
          override def nrow: F[Int] = mt.pure(result_set.size)
          override def row_iter: MonadicIterator[F, Table.Row] =
            MonadicIterator.from_iteratorf(result_set)
        })
      }

    } yield table

  }

}
