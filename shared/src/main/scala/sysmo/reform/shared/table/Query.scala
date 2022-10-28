package sysmo.reform.shared.table

import sysmo.reform.shared.expr.{ColumnRef, Expression, FieldRef, PredicateExpression}

/** # Filter */
sealed trait Projection
object Projection {
  case object All extends Projection
}
case class Columns(columns: Seq[ColumnRef]) extends Projection
case class Fields(fields: Seq[FieldRef]) extends Projection


/** # Filter */
case class QueryFilter(expr: PredicateExpression)

/** # Sort */
case class ColumnSort(col: ColumnRef, ascending: Boolean)

case class QuerySort(column_sorts: ColumnSort*)

/** # Source */
sealed trait QuerySource
case class SingleTable(id: String, alias: Option[String] = None, schema: Option[String] = None) extends QuerySource

/** # Range */
case class QueryRange(start: Int, length: Int)


/** # Query */
sealed trait Query

case class BasicQuery(
   source: QuerySource,
   projection: Projection = Projection.All,
   filter: Option[QueryFilter] = None,
   sort: Option[QuerySort] = None,
   range: Option[QueryRange] = None
) extends Query



