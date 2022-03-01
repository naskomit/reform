package sysmo.reform.shared.query
import upickle.default.{macroRW, read, readwriter, writeJs, ReadWriter => RW}
import ujson.Value

import scala.util.{Try, Success, Failure}

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

case class BasicQuery(source: QuerySource, filter: Option[QueryFilter], sort: Option[QuerySort], range: Option[QueryRange]) extends Query


object ReadersWriters {
  implicit val rwColumnRef: RW[ColumnRef] = macroRW
  implicit val rwValue: RW[Val] = readwriter[Value].bimap[Val](
    x => x.v match {
      case y: Int => y
      case y: Double => y
      case y: Boolean => y
      case y: String => y
      case y => throw new IllegalArgumentException(f"Cannot serialize value $y")
    },

    x => x.value match {
      case y: Int => Val(y)
      case y: Double => Val(y)
      case y: Boolean => Val(y)
      case y: String => Val(y)
      case y => throw new IllegalArgumentException(f"Cannot deserialize value $y")
    }
  )
//  implicit val rwRealValue: RW[RealValue] = macroRW
//  implicit val rwStringValue: RW[StringValue] = macroRW
//  implicit val rwBoolValue: RW[BoolValue] = macroRW
  implicit val rwLogicalAnd: RW[LogicalAnd] = macroRW
  implicit val rwLogicalOr: RW[LogicalOr] = macroRW
  implicit val rwLogicalNot: RW[LogicalNot] = macroRW

  implicit val rwNumericalPredicateOp: RW[NumericalPredicateOp.Value] =
    readwriter[String].bimap[NumericalPredicateOp.Value](_.toString, NumericalPredicateOp.withName)
  implicit val rwNumericalPredicate: RW[NumericalPredicate] = macroRW
  implicit val rwStringPredicateOp: RW[StringPredicateOp.Value] =
    readwriter[String].bimap[StringPredicateOp.Value](_.toString, StringPredicateOp.withName)
  implicit val rwStringPredicate: RW[StringPredicate] = macroRW
  implicit val rwContainmentPredicateOp: RW[ContainmentPredicateOp.Value] =
    readwriter[String].bimap[ContainmentPredicateOp.Value](_.toString, ContainmentPredicateOp.withName)
  implicit val rwContainmentPredicate: RW[ContainmentPredicate] = macroRW

  implicit val rwPredicateExpression: RW[PredicateExpression] = RW.merge(
    macroRW[NumericalPredicate],  macroRW[StringPredicate],  macroRW[ContainmentPredicate],
    macroRW[LogicalAnd], macroRW[LogicalOr], macroRW[LogicalNot]
  )

  implicit val rwExpression: RW[Expression] = readwriter[Value].bimap[Expression](
    {
      case x: PredicateExpression => writeJs(x)
      case x: ColumnRef => writeJs(x) //{val obj = ; obj("$type") = "ColumnRef"; obj }
      case x: Val => writeJs(x)
      case x => throw new IllegalArgumentException(f"Cannot serialize expression $x")
    },
    x => {
      Try {
        read[PredicateExpression](x)
      } orElse Try {
        read[ColumnRef](x)
      } orElse Try {
        read[Val](x)
      } match {
        case Success(v) => v.asInstanceOf[Expression]
        case Failure(e) => throw new IllegalArgumentException(f"Cannot parse expression $x")
      }

    }
  )

  implicit val rwQueryFilter: RW[QueryFilter] = macroRW
  implicit val rwColumnSort: RW[ColumnSort] = macroRW
  implicit val rwQuerySort: RW[QuerySort] = macroRW
  implicit val rwQuerySource: RW[QuerySource] = RW.merge(
    macroRW[SingleTable]
  )
  implicit val rwSingleTable: RW[SingleTable] = macroRW
  implicit val rwQueryRange: RW[QueryRange] = macroRW
  implicit val rwQuery: RW[Query] = RW.merge(
    macroRW[BasicQuery]
  )
  implicit val rwBasicQuery: RW[BasicQuery] = macroRW
}
