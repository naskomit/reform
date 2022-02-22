package sysmo.reform.shared.query


/** # Expression */
trait Expression

case class ColumnRef(id: String, alias: Option[String] = None, table: Option[String] = None) extends Expression

sealed trait AtomicValue extends Expression
case class RealValue(v: Double) extends AtomicValue
case class StringValue(v: String) extends AtomicValue
case class BoolValue(v: Boolean) extends AtomicValue

sealed trait PredicateExpression extends Expression

case class LogicalAnd(expr_list: Seq[PredicateExpression])
  extends PredicateExpression

case class LogicalOr(expr_list: Seq[PredicateExpression])
  extends PredicateExpression

case class LogicalNot(expr: PredicateExpression)
  extends PredicateExpression

object ComparisonOp extends Enumeration {
  type Comparison = Value
  val Equal, NotEqual, >, >=, <, <= = Value
}

case class LogicalComparison(op: ComparisonOp.Value, arg1: Expression, arg2: Expression)
  extends PredicateExpression

/** # Filter */
case class QueryFilter(expr: PredicateExpression)

/** # Sort */
case class ColumnSort(col: ColumnRef, ascending: true)

case class QuerySort(column_sorts: Seq[ColumnSort])

/** # Source */
trait QuerySource
case class SingleTable(id: String, alias: Option[String] = None, schema: Option[String] = None) extends QuerySource

/** # Range */
case class QueryRange(start: Int, length: Int)


/** # Query */
trait Query
case class BasicQuery(source: QuerySource, filter: Option[QueryFilter], sort: Option[QuerySort], range: Option[QueryRange]) extends Query
