package sysmo.reform.shared.query

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.expr.{ColumnRef, CommonPredicate, Constant, ContainmentPredicate, Equal, Expression, FieldRef, LogicalAnd, LogicalNot, LogicalOr, NP_<, NP_<=, NP_>, NP_>=, NotEqual, NumericalPredicate, PredicateExpression, StringPredicate, TypePredicateExpression}
import sysmo.reform.shared.types.RecordFieldType

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait SQLTextualGenerator[F[+_]] {
  implicit val mt: MonadThrow[F]
  val builder = new mutable.StringBuilder
  val arguments = new ArrayBuffer[Any]()

  /** Default quotation using back-quote */
  protected def quote(s: String): String = "`" + s + "`"

  protected def add_sequence(seq: Seq[String], sep: String): F[Unit] = {
    seq.zipWithIndex.foreach {
      case(word, index) => {
        builder.append(word)
        if (index < seq.length - 1) {
          builder.append(sep)
        }
      }
    }
    mt.pure()
  }

  def generate_select(model: SQLModel.SQL): F[Unit] = {
    mt.pure(
      model.select.columns.map(column => quote(column.id))
    ).map { proj =>
      builder.append("SELECT ")
      builder.append(proj.mkString(","))
    }
  }

  def generate_from(model: SQLModel.SQL): F[Unit] = {
    builder.append(" FROM ")
    builder.append(quote(model.from.table.id))
    mt.pure()
  }

  def generate_expr(expr: Expression): String = {
    import sysmo.reform.shared.data.Value
    import Value.implicits._
    expr match {
      case FieldRef(id, ftype) => quote(id)
      case Constant(v) => v.get[String].get
      case pred: PredicateExpression => pred match {
        case LogicalAnd(expr_list@_*) =>
          expr_list.map(sub => "(" + generate_expr(sub) + ")").mkString(" AND ")
        case LogicalOr(expr_list@_*) =>
          expr_list.map(sub => "(" + generate_expr(sub) + ")").mkString(" OR ")
        case LogicalNot(sub) => s"(NOT ${generate_expr(sub)})"
        case CommonPredicate(op, arg1, arg2) => op match {
          case Equal => s"${generate_expr(arg1)} = ${generate_expr(arg2)}"
          case NotEqual => s"${generate_expr(arg1)} != ${generate_expr(arg2)}"
        }
        case NumericalPredicate(op, arg1, arg2) => op match {
          case NP_> => s"${generate_expr(arg1)} > ${generate_expr(arg2)}"
          case NP_>= => s"${generate_expr(arg1)} >= ${generate_expr(arg2)}"
          case NP_< => s"${generate_expr(arg1)} < ${generate_expr(arg2)}"
          case NP_<= => s"${generate_expr(arg1)} <= ${generate_expr(arg2)}"
        }
        case StringPredicate(op, arg1, arg2) => ???
        case ContainmentPredicate(op, element, container) => ???
        case expression: TypePredicateExpression => ???
      }
    }
  }

  def generate_where(model: SQLModel.SQL): F[Unit] = {
    model.where.foreach {where =>
      builder.append(" WHERE ")
      builder.append(generate_expr(where.expr))
    }
    mt.pure()
  }

  def generate_order(model: SQLModel.SQL): F[Unit] = {
    model.order.foreach {order =>
      builder.append(" ORDER BY ")
      val words = order.column_orders.map{col_order =>
        quote(col_order.col) + (if (col_order.ascending) " ASC" else " DESC")
      }
      add_sequence(words, sep = ", ")
    }
    mt.pure()
  }

  def generate(model: SQLModel.SQL, ftypes: Seq[Option[RecordFieldType]]): F[SQLModel.TextualQuery] =
    for {
      _ <- generate_select(model)
      _ <- generate_from(model)
      _ <- generate_where(model)
      _ <- generate_order(model)
    } yield SQLModel.TextualQuery(builder.toString(), arguments.toSeq, ftypes)
}
