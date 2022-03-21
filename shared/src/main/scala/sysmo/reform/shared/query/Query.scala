package sysmo.reform.shared.query

/** # Expression */
sealed trait Expression

case class ColumnRef(id: String, alias: Option[String] = None, table: Option[String] = None) extends Expression

case class Val(v: Any) extends Expression
//case class RealValue(v: Double) extends Expression
//case class StringValue(v: String) extends Expression
//case class BoolValue(v: Boolean) extends Expression

sealed trait PredicateExpression extends Expression

case class LogicalAnd(expr_list: PredicateExpression*)
  extends PredicateExpression

case class LogicalOr(expr_list: PredicateExpression*)
  extends PredicateExpression

case class LogicalNot(expr: PredicateExpression)
  extends PredicateExpression

trait PredicateOp extends Enumeration

object NumericalPredicateOp extends PredicateOp {
  type NumericalPredicateOp = Value
  val Equal, NotEqual, >, >=, <, <= = Value
}

case class NumericalPredicate(op: NumericalPredicateOp.Value, arg1: Expression, arg2: Expression)
  extends PredicateExpression

object StringPredicateOp extends PredicateOp {
  type StringPredicateOp = Value
  val Equal, NotEqual, StartingWith, NonStartingWith, EndingWith, NotEndingWith, Containing, NotContaining = Value
}

case class StringPredicate(op: StringPredicateOp.Value, arg1: Expression, arg2: Expression)
  extends PredicateExpression

object ContainmentPredicateOp extends PredicateOp {
  val Within, Without = Value
}

case class ContainmentPredicate(op: ContainmentPredicateOp.Value, arg1: ColumnRef, arg2: Seq[Val])
  extends PredicateExpression

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



