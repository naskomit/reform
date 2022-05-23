package sysmo.reform.shared.expr

import cats.implicits.{catsSyntaxPartialOrder, catsSyntaxTuple2Semigroupal}

import scala.reflect.ClassTag

/** # Expression */
sealed trait Expression {
  def ===(other: Expression): PredicateExpression = {
    CommonPredicate(CommonPredicateOp.Equal, this, other)
  }
  def !==(other: Expression): PredicateExpression = {
    CommonPredicate(CommonPredicateOp.NotEqual, this, other)
  }

  def <(other: Expression): PredicateExpression = {
    NumericalPredicate(NumericalPredicateOp.<, this, other)
  }
  def <=(other: Expression): PredicateExpression = {
    NumericalPredicate(NumericalPredicateOp.<=, this, other)
  }
  def >(other: Expression): PredicateExpression = {
    NumericalPredicate(NumericalPredicateOp.>, this, other)
  }
  def >=(other: Expression): PredicateExpression = {
    NumericalPredicate(NumericalPredicateOp.>=, this, other)
  }

  def this_expr: Expression = this

  object str {
    def starting_with(other: Expression): PredicateExpression =
      StringPredicate(StringPredicateOp.StartingWith, this_expr, other)
    def non_starting_with(other: Expression): PredicateExpression =
      StringPredicate(StringPredicateOp.NonStartingWith, this_expr, other)
    def ending_with(other: Expression): PredicateExpression =
      StringPredicate(StringPredicateOp.EndingWith, this_expr, other)
    def not_ending_with(other: Expression): PredicateExpression =
      StringPredicate(StringPredicateOp.NotEndingWith, this_expr, other)
    def containing(other: Expression): PredicateExpression =
      StringPredicate(StringPredicateOp.Containing, this_expr, other)
    def not_containing(other: Expression): PredicateExpression =
      StringPredicate(StringPredicateOp.NotContaining, this_expr, other)
  }

}

object Expression {
  type Result[T] = Either[EvalError, T]

  implicit class ResOps[T](x: Result[T]) {
    def to_error: Throwable = x.left.getOrElse(new Throwable())
  }

  trait Converter[T, U] {
    def convert(x: T): U
  }

//  def try_convert[T, U](v: U)(implicit cnv: Converter[T, U]): Result[T] = v match {
//
//  }

  def as[T](v: Any)(implicit ev: ClassTag[T]): Result[T] = v match {
    case x: T => Right(x)
    case _ => Left(IncorrectTypeError(v, ev.runtimeClass.getName))
  }

  def as_float(v: Result[_]): Result[Double] = v match {
    case Left(e) => Left(e)
    case Right(v: Double) => Right(v)
    case Right(v: Float) => Right(v)
    case Right(v: Int) => Right(v)
    case Right(v: Long) => Right(v)
    case _ => Left(ConversionError(v, Double.getClass.getName))
  }
  type CmpFn = (Double, Double) => Boolean
  object compare_fns {
    val < : CmpFn = (x, y) => x < y
    val <= : CmpFn = (x, y) => x <= y
    val > : CmpFn = (x, y) => x > y
    val >= : CmpFn = (x, y) => x >= y
  }

  def num_compare(arg1: Expression, arg2: Expression, cmp_fn: CmpFn, ctx: Context): Result[Boolean] = {
    (as_float(eval(arg1, ctx)), as_float(eval(arg2, ctx))) match {
      case (Right(x), Right(y)) => Right(cmp_fn(x, y))
      case (Left(e), _) => Left(e)
      case (_, Left(e)) => Left(e)
    }
  }

  def eval[T](expr: Expression, ctx: Context)(implicit ev: ClassTag[T]): Result[T]  = {
    expr match {
      case Constant(v) => as[T](v)
      case FieldRef(id) => ctx.get(id).toRight(NoSuchFieldError(id))
        .flatMap(as[T](_))
      case CommonPredicate(op, arg1, arg2) => op match {
        case CommonPredicateOp.Equal => as[T](eval(arg1, ctx) == eval(arg2, ctx))
        case CommonPredicateOp.NotEqual => as[T](eval(arg1, ctx) != eval(arg2, ctx))
      }
      case NumericalPredicate(op, arg1, arg2) => op match {
        case NumericalPredicateOp.< => num_compare(arg1, arg2, compare_fns.<, ctx).flatMap(as[T])
        case NumericalPredicateOp.<= => num_compare(arg1, arg2, compare_fns.<=, ctx).flatMap(as[T])
        case NumericalPredicateOp.> => num_compare(arg1, arg2, compare_fns.>, ctx).flatMap(as[T])
        case NumericalPredicateOp.>= => num_compare(arg1, arg2, compare_fns.>=, ctx).flatMap(as[T])
      }
    }
  }

  def apply(x: Any): Expression = Constant(x)
  def col(id: String): Expression = ColumnRef(id)
}

/** # Errors */
trait EvalError extends Throwable
case class IncorrectTypeError(obj: Any, cls: String) extends EvalError {
  override def toString: String = s"$obj is not an instance of ${cls}"
}
case class ConversionError(obj: Any, cls: String) extends EvalError {
  override def toString: String = s"Cannot convert $obj to ${cls}"
}
case class NoSuchFieldError(id: String) extends EvalError {
  override def toString: String = s"Unknow field $id"
}

/** # Expression subclasses */
case class ColumnRef(id: String, alias: Option[String] = None, table: Option[String] = None) extends Expression
case class FieldRef(id: String) extends Expression

case class Constant(v: Any) extends Expression
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

object CommonPredicateOp extends PredicateOp {
  type CommonPredicateOp = Value
  val Equal, NotEqual = Value
}

case class CommonPredicate(op: CommonPredicateOp.Value, arg1: Expression, arg2: Expression)
  extends PredicateExpression

object NumericalPredicateOp extends PredicateOp {
  type NumericalPredicateOp = Value
  val >, >=, <, <= = Value
}

case class NumericalPredicate(op: NumericalPredicateOp.Value, arg1: Expression, arg2: Expression)
  extends PredicateExpression

object StringPredicateOp extends PredicateOp {
  type StringPredicateOp = Value
  val StartingWith, NonStartingWith, EndingWith, NotEndingWith, Containing, NotContaining = Value
}

case class StringPredicate(op: StringPredicateOp.Value, arg1: Expression, arg2: Expression)
  extends PredicateExpression

object ContainmentPredicateOp extends PredicateOp {
  val Within, Without = Value
}

case class ContainmentPredicate(op: ContainmentPredicateOp.Value, arg1: ColumnRef, arg2: Seq[Constant])
  extends PredicateExpression

trait Context extends Map[String, Any]

object Context {
  def apply(base: Map[String, Any]): Context = new Context {
    override def removed(key: String): Map[String, Any] = base.removed(key)
    override def updated[V1 >: Any](key: String, value: V1): Map[String, V1] = base.updated(key, value)
    override def get(key: String): Option[Any] = base.get(key)
    override def iterator: Iterator[(String, Any)] = base.iterator
  }
}

