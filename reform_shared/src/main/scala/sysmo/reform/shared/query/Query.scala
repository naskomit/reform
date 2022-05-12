package sysmo.reform.shared.query

import sysmo.reform.shared.expr.{ColumnRef, Expression, PredicateExpression}

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
   columns: Option[Seq[ColumnRef]] = None,
   filter: Option[QueryFilter] = None,
   sort: Option[QuerySort] = None,
   range: Option[QueryRange] = None
) extends Query



