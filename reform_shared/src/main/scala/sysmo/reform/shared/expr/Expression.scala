package sysmo.reform.shared.expr

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

  def within(container: Seq[Constant]): ContainmentPredicate = {
    ContainmentPredicate(ContainmentPredicateOp.Within, this, container)
  }

  def without(container: Seq[Constant]): ContainmentPredicate = {
    ContainmentPredicate(ContainmentPredicateOp.Without, this, container)
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

  implicit class ResOps[T](x: Result[T]) {
    def to_error: Throwable = x.left.getOrElse(new Throwable())
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

  def num_compare(arg1: Expression, arg2: Expression, cmp_fn: CmpFn, ctx: Context[_]): Result[Boolean] = {
    (as_float(eval(arg1, ctx)), as_float(eval(arg2, ctx))) match {
      case (Right(x), Right(y)) => Right(cmp_fn(x, y))
      case (Left(e), _) => Left(e)
      case (_, Left(e)) => Left(e)
    }
  }

  def bool_eval_until(expr_list: List[PredicateExpression], until: Boolean, ctx: Context[_]): Result[Boolean] = {
    if (expr_list.isEmpty)
      Right(!until)
    else {
      val (head :: tail) = expr_list
      eval(head, ctx).flatMap {
        case x: Boolean if x == until => Right(until)
        case x: Boolean if x != until => bool_eval_until(tail, until, ctx)
        case x => Left(IncorrectTypeError(x, Boolean.getClass.getName))
      }

    }
  }

  def eval_seq(s: Seq[Expression], ctx: Context[_]): Result[Seq[Any]] = {
    s.foldLeft[Result[Seq[Any]]](Right(Seq[Any]())) {(acc, item) =>
      acc match {
        case Left(err) => Left(err)
        case Right(vs) => eval(item, ctx) match {
          case Left(err) => Left(err)
          case Right(item_value) => Right(vs :+ item_value)
        }
      }
    }
  }

  def eval(expr: Expression, ctx: Context[_]): Result[Any]  = {
    expr match {
      case Constant(v) => Right(v)
      case FieldRef(id) => ctx.get(id).toRight(NoSuchFieldError(id))
      case CommonPredicate(op, arg1, arg2) => op match {
        case CommonPredicateOp.Equal => Right(eval(arg1, ctx) == eval(arg2, ctx))
        case CommonPredicateOp.NotEqual => Right(eval(arg1, ctx) != eval(arg2, ctx))
      }
      case NumericalPredicate(op, arg1, arg2) => op match {
        case NumericalPredicateOp.< => num_compare(arg1, arg2, compare_fns.<, ctx)
        case NumericalPredicateOp.<= => num_compare(arg1, arg2, compare_fns.<=, ctx)
        case NumericalPredicateOp.> => num_compare(arg1, arg2, compare_fns.>, ctx)
        case NumericalPredicateOp.>= => num_compare(arg1, arg2, compare_fns.>=, ctx)
      }
      case LogicalAnd(expr_list @ _*) => bool_eval_until(expr_list.toList, until = false, ctx)
      case LogicalOr(expr_list @ _*) => bool_eval_until(expr_list.toList, until = true, ctx)
      case LogicalNot(expr) => eval(expr, ctx).flatMap {
        case x: Boolean => Right(!x)
        case x => Left(IncorrectTypeError(x, Boolean.getClass.getName))
      }
      case ContainmentPredicate(op, element, container) => {
        eval(element, ctx) match {
          case Right(elem_value) => eval_seq(container, ctx)
            .map {x =>
              val contains = x.toSet.contains(elem_value)
              (op == ContainmentPredicateOp.Within && contains) ||
                (op == ContainmentPredicateOp.Without && !contains)
            }
          case Left(err) => Left(err)
        }
      }
    }
  }

  def apply[T](x: T): Constant = Constant(x)
  def col(id: String): ColumnRef = ColumnRef(id)
  def field(id: String): FieldRef = FieldRef(id)
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

case class ContainmentPredicate(op: ContainmentPredicateOp.Value, element: Expression, container: Seq[Constant])
  extends PredicateExpression

trait Context[+T] extends Map[String, T]

object Context {
  def apply[T](base: Map[String, T]): Context[T] = new Context[T] {
    override def removed(key: String): Map[String, T] = base.removed(key)
    override def updated[V1 >: T](key: String, value: V1): Map[String, V1] = base.updated(key, value)
    override def get(key: String): Option[T] = base.get(key)
    override def iterator: Iterator[(String, T)] = base.iterator
  }
}

