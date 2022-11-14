package sysmo.reform.shared.query

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.query.SQLModel.ColumnOrder
import sysmo.reform.shared.types.RecordFieldType

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait SQLGenerator[F[+_]] {
  implicit val mt: MonadThrow[F]
  val textual_generator: SQLTextualGenerator[F]

  def generate_ftypes(projection: Projection): F[Seq[Option[RecordFieldType]]] = {
    projection match {
      case Fields(fields) => mt.pure(fields.map(_.ftype))
      case _ => mt.raiseError(new IllegalArgumentException("OrientDB query engine only works with field references"))
    }
  }

  def generate_select(projection: Projection): F[SQLModel.Select] = {
    (projection match {
      //        case Projection.All => mt.pure(Seq("*"))
      //        case Columns(columns) => mt.pure(columns.map(cr => quote(cr.id)))
      case Fields(fields) => mt.pure(fields.map(f => SQLModel.Column(f.id, None, None)))
      case _ => mt.raiseError(new IllegalArgumentException("OrientDB query engine only works with field references"))
    }).map { proj => SQLModel.Select(proj)}
  }

  def generate_from(source: QuerySource): F[SQLModel.From] = {
    (source match {
      case x: SingleTable => mt.pure(SQLModel.Table(x.id))
      case _ => mt.raiseError(new IllegalArgumentException("Can only process SingleTable source"))
    }).map {src => SQLModel.From(src)}
  }

  def generate_order(sort: Option[QuerySort]): F[Option[SQLModel.Order]] = {
    mt.pure(sort.map {
      case QuerySort(column_sorts) => column_sorts.map {
        case ColumnSort(col, ascending) => ColumnOrder(col.id, ascending)
      }
    }.map { x => SQLModel.Order(x) })
  }

  def from_basic_query(q: BasicQuery): F[SQLModel.TextualQuery] = {
    for {
      ftypes <- generate_ftypes(q.projection)
      select <- generate_select(q.projection)
      order <- generate_order(q.sort)
      from <- generate_from(q.source)
      sql <- {
        val where = q.filter.map(flt => SQLModel.Where(flt.expr))
        val sql_model = SQLModel.SQL(select, from, where, order, ftypes)
        textual_generator.generate(sql_model, ftypes)
      }
    } yield sql
  }
}
