package sysmo.reform.shared.expr

import sysmo.reform.shared.data.{Value, ValueConstructor}
import sysmo.reform.shared.types.RecordFieldType

/** # Expression subclasses */
sealed trait Expression

case class ColumnRef(id: String, alias: Option[String] = None, table: Option[String] = None) extends Expression
case class FieldRef(id: String, ftype: Option[RecordFieldType] = None) extends Expression
case class Constant(v: Value) extends Expression

sealed trait PredicateExpression extends Expression {
  def && (other: PredicateExpression): PredicateExpression = {
    LogicalAnd(this, other)
  }
  def || (other: PredicateExpression): PredicateExpression = {
    LogicalOr(this, other)
  }

  def not: PredicateExpression = {
    LogicalNot(this)
  }

}

case class LogicalAnd(expr_list: PredicateExpression*)
  extends PredicateExpression

case class LogicalOr(expr_list: PredicateExpression*)
  extends PredicateExpression

case class LogicalNot(expr: PredicateExpression)
  extends PredicateExpression

/** Predicates */
sealed trait PredicateOp

/** Common Predicates */
sealed trait CommonPredicateOp extends PredicateOp
case object Equal extends CommonPredicateOp
case object NotEqual extends CommonPredicateOp

case class CommonPredicate(op: CommonPredicateOp, arg1: Expression, arg2: Expression)
  extends PredicateExpression

/** Numerical Predicates */
sealed trait NumericalPredicateOp extends PredicateOp
case object NP_> extends NumericalPredicateOp
case object NP_>= extends NumericalPredicateOp
case object NP_< extends NumericalPredicateOp
case object NP_<= extends NumericalPredicateOp

case class NumericalPredicate(op: NumericalPredicateOp, arg1: Expression, arg2: Expression)
  extends PredicateExpression

/** String Predicates */
sealed trait StringPredicateOp extends PredicateOp
case object StartingWith extends StringPredicateOp
case object NonStartingWith extends StringPredicateOp
case object EndingWith extends StringPredicateOp
case object NotEndingWith extends StringPredicateOp
case object Containing extends StringPredicateOp
case object NotContaining extends StringPredicateOp

case class StringPredicate(op: StringPredicateOp, arg1: Expression, arg2: Expression)
  extends PredicateExpression

/** Containment Predicates */
sealed trait ContainmentPredicateOp extends PredicateOp
case object Within extends ContainmentPredicateOp
case object Without extends ContainmentPredicateOp

case class ContainmentPredicate(op: ContainmentPredicateOp, element: Expression, container: Seq[Constant])
  extends PredicateExpression

/** DType predicates */
sealed trait DTypeExpression

//sealed trait PrimitiveType
//
//case object Real extends PrimitiveType
//case object Int extends PrimitiveType
//case object Long extends PrimitiveType
//case object Char extends PrimitiveType
//case object Bool extends PrimitiveType
//case object Date extends PrimitiveType
//case object Id extends PrimitiveType

//case class Primitive(primitive_type: Option[PrimitiveType] = None) extends DTypeExpression

case class Record(symbol: Option[String] = None) extends DTypeExpression
case class Array(prototype: Option[String] = None) extends DTypeExpression
//case class Reference(prototype: Option[String] = None) extends DTypeExpression
//case class MultiReference(prototype: Option[String] = None) extends DTypeExpression

case class HasDType(dtype: DTypeExpression) extends TypePredicateExpression
sealed trait TypePredicateExpression extends PredicateExpression

object Expression {
  def apply(v: Value): Constant = Constant(v)
  def apply[T](x: T)(implicit vc: ValueConstructor[T]): Constant =
    Constant(vc.toValue(x))
  def col(id: String): ColumnRef = ColumnRef(id)
  def field(id: String, ftype: Option[RecordFieldType] = None): FieldRef = FieldRef(id, ftype)

  object implicits {
    /** # Expression */
    implicit class ExpressionBuilder(expr: Expression) {
      def ===(other: Expression): PredicateExpression = {
        CommonPredicate(Equal, expr, other)
      }

      def !==(other: Expression): PredicateExpression = {
        CommonPredicate(NotEqual, expr, other)
      }

      def <(other: Expression): PredicateExpression = {
        NumericalPredicate(NP_<, expr, other)
      }

      def <=(other: Expression): PredicateExpression = {
        NumericalPredicate(NP_<=, expr, other)
      }

      def >(other: Expression): PredicateExpression = {
        NumericalPredicate(NP_>, expr, other)
      }

      def >=(other: Expression): PredicateExpression = {
        NumericalPredicate(NP_>=, expr, other)
      }

      def within(container: Seq[Constant]): ContainmentPredicate = {
        ContainmentPredicate(Within, expr, container)
      }

      def without(container: Seq[Constant]): ContainmentPredicate = {
        ContainmentPredicate(Without, expr, container)
      }

      object str {
        def starting_with(other: Expression): PredicateExpression =
          StringPredicate(StartingWith, expr, other)
        def non_starting_with(other: Expression): PredicateExpression =
          StringPredicate(NonStartingWith, expr, other)
        def ending_with(other: Expression): PredicateExpression =
          StringPredicate(EndingWith, expr, other)
        def not_ending_with(other: Expression): PredicateExpression =
          StringPredicate(NotEndingWith, expr, other)
        def containing(other: Expression): PredicateExpression =
          StringPredicate(Containing, expr, other)
        def not_containing(other: Expression): PredicateExpression =
          StringPredicate(NotContaining, expr, other)
      }

    }
  }
}


