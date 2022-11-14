package sysmo.reform.shared.query

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.expr.{Containing, EndingWith, Expression, NonStartingWith, NotContaining, NotEndingWith, StartingWith}
import sysmo.reform.shared.query.Enclosure.Brackets
import sysmo.reform.shared.types.RecordFieldType

case class SQLTextualQuery(q: String, args: Seq[Any], ftypes: Seq[Option[RecordFieldType]])

trait SQLGenerator[F[+_]] {
  implicit val mt: MonadThrow[F]
  val T = TextualTreeNode
  object K {
    val SELECT = "SELECT"
    val FROM = "FROM"
    val WHERE = "WHERE"
    val ORDER = "ORDER BY"
    val AND = "AND"
    val OR = "OR"
    val NOT = "NOT"
    val ASC = "ASC"
    val DESC = "DESC"
    val LIKE = "LIKE"
  }

  def generate_ftypes(projection: Projection): F[Seq[Option[RecordFieldType]]] = {
    projection match {
      case Fields(fields) => mt.pure(fields.map(_.ftype))
      case _ => mt.raiseError(new IllegalArgumentException("OrientDB query engine only works with field references"))
    }
  }

  protected def quote_field(s: String): String = "`" + s + "`"
  protected def quote_str(s: String): String = "\"" + s + "\""

  protected def format_field(path: Seq[String], alias: Option[String] = None): String = {
    quote_field(path.mkString(".")) + (alias match {
      case Some(v) => s" AS ${quote_field(v)}"
      case None => ""
    })
  }

  def generate_select(projection: Projection): F[TextualTreeNode] = {
    val proj_txt = T.node(", ")
    (projection match {
      case Projection.All => proj_txt += T.leaf("*")
      case Fields(fields) => fields.foreach { field =>
        val alias = field.ftype.map(ft => ft.name)
        proj_txt += T.leaf(format_field(field.path, alias))
      }
      case _ => mt.raiseError(new IllegalArgumentException("OrientDB query engine only works with field references"))
    })
    mt.pure(proj_txt)
  }

  def generate_from(source: QuerySource): F[TextualTreeNode] = {
    val source_txt = T.node(", ")
    source match {
      case table: SingleTable => source_txt += T.leaf(table.id)
      case _ => mt.raiseError(new IllegalArgumentException("Can only process SingleTable source"))
    }
    mt.pure(source_txt)
  }

  def generate_order(sort: Option[QuerySort]): F[TextualTreeNode] = {
    val order_txt = T.node(", ")

    sort match {
      case Some(QuerySort(field_sorts)) => field_sorts.foreach {
        case FieldSort(field, ascending) =>
          order_txt += (
            T.leaf(format_field(field.path) +
              (if (ascending) s" ${K.ASC}" else s" ${K.DESC}")))
      }
      case None =>
    }
    mt.pure(order_txt)
  }

  private def binary_predicate(expr1: Expression, op: TextualTreeNode, expr2: Expression): F[TextualTreeNode] =
    mt.map2(generate_filter(expr1), generate_filter(expr2))((n1, n2) =>
      T.node(" ", Brackets) += n1 += op += n2)

  private def not_expr(expr_node: F[TextualTreeNode]): F[TextualTreeNode] =
    expr_node.map(n =>
      T.node(" ", Brackets) += T.leaf(K.NOT) += n
    )

  def generate_filter(expr: Expression): F[TextualTreeNode] = {
    import sysmo.reform.shared.expr.{CommonPredicate, Constant, ContainmentPredicate, Equal, Expression, FieldRef, LogicalAnd, LogicalNot, LogicalOr, NP_<, NP_<=, NP_>, NP_>=, NotEqual, NumericalPredicate, PredicateExpression, StringPredicate, TypePredicateExpression}
    import sysmo.reform.shared.data.Value
    import Value.implicits._

    expr match {
      case FieldRef(path, _) => mt.pure(T.leaf(quote_field(path.mkString("."))))
      case Constant(v) => v match {
        case v if v.not_set => mt.raiseError(new IllegalArgumentException("Cannot handle empty values"))
        case Value.CharValue(x) => mt.pure(T.leaf(quote_str(v.get[String].get)))
        case _ => mt.pure(T.leaf(v.get[String].get))
      }
      case pred: PredicateExpression => pred match {
        case LogicalAnd(expr_list@_*) => expr_list.foldLeft(
          mt.pure(T.node(s" ${K.AND} ", Brackets))
        ) { (parentF, sub) =>
            mt.map2(parentF, generate_filter(sub))((parent, n) => parent += n)
          }
        case LogicalOr(expr_list@_*) => expr_list.foldLeft(
            mt.pure(T.node(s" ${K.OR} ", Brackets))
          ) { (parentF, sub) =>
            mt.map2(parentF, generate_filter(sub))((parent, n) => parent += n)
          }
        case LogicalNot(sub) => not_expr(generate_filter(sub))
        case CommonPredicate(op, arg1, arg2) => op match {
          case Equal => binary_predicate(arg1, T.leaf("="), arg2)
          case NotEqual => binary_predicate(arg1, T.leaf("!="), arg2)
        }
        case NumericalPredicate(op, arg1, arg2) => op match {
          case NP_> => binary_predicate(arg1, T.leaf(">"), arg2)
          case NP_>= => binary_predicate(arg1, T.leaf(">="), arg2)
          case NP_< => binary_predicate(arg1, T.leaf("<"), arg2)
          case NP_<= => binary_predicate(arg1, T.leaf("<="), arg2)
        }
        case StringPredicate(op, arg1, Constant(sc)) => {
          val arg2 = sc.get[String].get
          op match {
            case StartingWith => binary_predicate(arg1, T.leaf(K.LIKE), Expression(arg2 + "%"))
            case NonStartingWith => not_expr(
              binary_predicate(arg1, T.leaf(K.LIKE), Expression(arg2 + "%"))
            )
            case EndingWith => binary_predicate(arg1, T.leaf(K.LIKE), Expression("%" + arg2))
            case NotEndingWith => not_expr(
              binary_predicate(arg1, T.leaf(K.LIKE), Expression("%" + arg2))
            )
            case Containing => binary_predicate(arg1, T.leaf(K.LIKE), Expression("%" + arg2 + "%"))
            case NotContaining => not_expr(
              binary_predicate(arg1, T.leaf(K.LIKE), Expression("%" + arg2 + "%"))
            )
          }
        }

        case sp @ StringPredicate(op, arg1, arg2) =>
          mt.raiseError(new IllegalArgumentException(
            "Last argument to string predicate must be a constant string"
          ))
        case ContainmentPredicate(op, element, container) => ???
        case expression: TypePredicateExpression => ???
      }
    }
  }

  def from_basic_query(q: BasicQuery): F[SQLTextualQuery] = {
    for {
      ftypes <- generate_ftypes(q.projection)
      select <- generate_select(q.projection)
      from <- generate_from(q.source)
      order <- generate_order(q.sort)
      filter <- q.filter.map(flt => generate_filter(flt.expr))
        .getOrElse(mt.pure(T.empty))
      query_txt <- {
        val query_txt = T.node(" ")
        query_txt += T.leaf(K.SELECT)
        query_txt += select
        query_txt += T.leaf(K.FROM)
        query_txt += from
        if (filter.has_children) {
          query_txt += T.leaf(K.WHERE)
          query_txt += filter
        }
        if (order.has_children) {
          query_txt += T.leaf(K.ORDER)
          query_txt += order
        }
        mt.pure(query_txt)
      }
      sql <- {
        val sql_txt = TreeTextualGenerator(query_txt).generate()
        mt.pure(
          SQLTextualQuery(sql_txt, Seq(), ftypes)
        )

      }
    } yield sql
  }

  def generate(q: Query): F[SQLTextualQuery] = {
    q match {
      case qb: BasicQuery => from_basic_query(qb)
      case _ => mt.raiseError(new IllegalArgumentException("Can only process BasicQuery"))
    }

  }
}
