package sysmo.reform.storage.orientdb

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.data.{Value}
import sysmo.reform.shared.query.{BasicQuery, Fields, Query, SQLQueryService, SQLQuery, SingleTable}
import sysmo.reform.shared.table.Table.Schema
import sysmo.reform.shared.table.{Table}
import sysmo.reform.shared.types.RecordType
import sysmo.reform.shared.util.MonadicIterator

import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class OrientDBQueryService[_F[+_]](session: SessionImpl[_F])(
  implicit val mt: MonadThrow[_F]) extends SQLQueryService[_F] {
  private def quote(s: String): String = "`" + s + "`"


  override def list_tables(): F[Seq[Schema]] = mt.pure(Seq())

  // TODO
  override def table_schema(table_id: String): F[Schema] = ???

  override def generate_sql(q: Query): F[SQLQuery] = {
    val builder = new mutable.StringBuilder
    val arguments = new ArrayBuffer[Any]()

    for {
      qbasic <- q match {
        case x: BasicQuery => mt.pure(x)
        case _ => mt.raiseError(new IllegalArgumentException("Can only process BasicQuery"))
      }

      projection <- qbasic.projection match {
//        case Projection.All => mt.pure(Seq("*"))
//        case Columns(columns) => mt.pure(columns.map(cr => quote(cr.id)))
        case Fields(fields) => mt.pure(fields.map(f => quote(f.id)))
        case _ => mt.raiseError(new IllegalArgumentException("OrientDB query engine only works with field references"))
      }

      ftypes <- qbasic.projection match {
        case Fields(fields) => mt.pure(fields.map(_.ftype))
        case _ => mt.raiseError(new IllegalArgumentException("OrientDB query engine only works with field references"))
      }

      source <- qbasic.source match {
        case x: SingleTable => mt.pure(quote(x.id))
        case _ => mt.raiseError(new IllegalArgumentException("Can only process SingleTable source"))
      }

      _ <- {
        builder.append("select ")
        builder.append(projection.mkString(","))
        builder.append(" from ")
        builder.append(source)
        mt.pure()
      }
    } yield SQLQuery(builder.toString(), arguments.toSeq, ftypes)
  }

  override def run_query(sql: SQLQuery): F[Table[F]] = {
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
//      result_set <- {
//        Util.catch_exception(
//        )
//      }

      table <- {
        val _mt = mt
        val result_set: Iterator[F[Table.Row]]  = session.db_session.query(sql.q, sql.args:_*).asScala
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
