package sysmo.reform.shared.query

import sysmo.reform.shared.expr.{FieldRef, PredicateExpression}

/** # Filter */
sealed trait Projection
object Projection {
  case object All extends Projection
}
case class Fields(fields: Seq[FieldRef]) extends Projection


/** # Filter */
case class QueryFilter(expr: PredicateExpression)

/** # Sort */
case class FieldSort(field: FieldRef, ascending: Boolean)

case class QuerySort(field_sorts: Seq[FieldSort])

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



