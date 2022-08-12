package sysmo.reform.shared.expr

import cats.MonadThrow
import sysmo.reform.shared.data.{Value, ValueExtractor}

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

/** Evaluation */
trait Evaluator[_F[+_]] {
  /** Types */
  type F[+X] = _F[X]
  type EContext = Context[F]
  /** Begin requirements */
  val mt: MonadThrow[F]
  import Value.implicits._
  /** End requirements */


  def traverse[T](as: Seq[F[T]])(f: Seq[T] => T): F[T] = {
    mt.map(
      as.foldLeft(mt.pure(Seq[T]())){(acc: F[Seq[T]], item: F[T]) =>
        mt.map2(acc, item)((acc_v: Seq[T], item_v: T) => acc_v :+ item_v)
      }
    )(f)
}

  def as_float(v: F[Value]): F[Double] = mt.flatMap(v)(_.get[Double] match {
    case Some(x) => mt.pure(x)
    case None => mt.raiseError(ConversionError(v, Double.getClass.getName))
  })

  def num_compare(arg1: Expression, arg2: Expression, cmp_fn: Evaluator.CmpFn, ctx: EContext): F[Value] = {
    mt.map2(as_float(eval(arg1, ctx)), as_float(eval(arg2, ctx)))((x, y) => cmp_fn(x, y))
  }

  def bool_eval_until(expr_list: List[PredicateExpression], until: Boolean, ctx: EContext): F[Value] = {
    if (expr_list.isEmpty)
      mt.pure(!until)
    else {
      val (head :: tail) = expr_list
      mt.flatMap(eval(head, ctx))(x => x.get[Boolean] match {
        case Some(x) if x == until => mt.pure(until)
        case Some(x) if x != until => bool_eval_until(tail, until, ctx)
        case x => mt.raiseError(IncorrectTypeError(x, Boolean.getClass.getName))
      })
    }
  }

  def eval(expr: Expression, ctx: EContext): F[Value] = {
    expr match {
      case Constant(v) => mt.pure(v)
      case ColumnRef(id, alias, table) => ???
      case FieldRef(id) => ctx.get(id)
      case pred_expr: PredicateExpression => pred_expr match {
        case LogicalAnd(expr_list@_*) => bool_eval_until(expr_list.toList, until = false, ctx)
        case LogicalOr(expr_list@_*) => bool_eval_until(expr_list.toList, until = true, ctx)
        case LogicalNot(expr) => mt.flatMap(eval(expr, ctx))(x => x.get[Boolean] match {
          case Some(x) => mt.pure(!x)
          case x => mt.raiseError(IncorrectTypeError(x, Boolean.getClass.getName))
        })

        case CommonPredicate(op, arg1, arg2) => op match {
          case Equal => mt.map2(eval(arg1, ctx), eval(arg2, ctx))((x, y) => x == y)
          case NotEqual => mt.map2(eval(arg1, ctx), eval(arg2, ctx))((x, y) => x != y)
        }

        case NumericalPredicate(op, arg1, arg2) => op match {
          case NP_< => num_compare(arg1, arg2, Evaluator.compare_fns.<, ctx)
          case NP_<= => num_compare(arg1, arg2, Evaluator.compare_fns.<=, ctx)
          case NP_> => num_compare(arg1, arg2, Evaluator.compare_fns.>, ctx)
          case NP_>= => num_compare(arg1, arg2, Evaluator.compare_fns.>=, ctx)
        }
//        case StringPredicate(op, arg1, arg2) => ???
//        case ContainmentPredicate(op, element, container) => ???
        //      case ContainmentPredicate(op, element, container) => {
        //        eval(element, ctx) match {
        //          case Right(elem_value) => eval_seq(container, ctx)
        //            .map {x =>
        //              val contains = x.toSet.contains(elem_value)
        //              (op == ContainmentPredicateOp.Within && contains) ||
        //                (op == ContainmentPredicateOp.Without && !contains)
        //            }
        //          case Left(err) => Left(err)
        //        }
        //      }
        //    }

      }
    }
  }

}

object Evaluator {
  type CmpFn = (Double, Double) => Boolean
  object compare_fns {
    val < : CmpFn = (x, y) => x < y
    val <= : CmpFn = (x, y) => x <= y
    val > : CmpFn = (x, y) => x > y
    val >= : CmpFn = (x, y) => x >= y
  }

  def apply[F[+_]](implicit _mt: MonadThrow[F]): Evaluator[F] = new Evaluator[F] {
    /** Begin requirements */
    override val mt = _mt
  }
}
