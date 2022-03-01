package sysmo.reform.shared.query

import sysmo.reform.shared.gremlin.bytecode.{Symbols => S}
import sysmo.reform.shared.gremlin.{bytecode => bc}

import scala.collection.mutable

object Query2GremlinCompiler {
  def compile(q: Query): bc.Bytecode = {
    q match {
      case BasicQuery(source, filter, sort, range) => {

        // Create bytecode step buffer and add source
        val steps = mutable.ArrayBuffer[bc.Instruction](
          compile_source(source): _*
        )
        // Add filters
        filter match {
          case Some(flt) => steps += compile_filter_expr(flt.expr)
          case None =>
        }

        // Return more detailed output
        // steps += Instruction(S.with_, Seq("~tinkerpop.valueMap.tokens", 15))

        // Sort
        sort match {
          case Some(sort_) => sort_.column_sorts.reverse.foreach(cs => {
            val dir = if (cs.ascending) bc.Order.asc else bc.Order.desc
            steps ++= Seq(
              bc.Instruction(S.order), bc.Instruction(S.by, cs.col.id, dir)
            )
          })
          case None =>
        }

        // Limit to range
        range match {
          case Some(range_) => steps += bc.Instruction(S.range, range_.start, range_.start + range_.length)
          case None =>
        }

        // Convert to value map
        steps += bc.Instruction(S.valueMap)

        // Return bytecode
        bc.Bytecode(step = steps.toSeq)
      }

      case _ => throw new IllegalArgumentException(f"Cannot compile query $q to Gremlin")
    }
  }

  def compile_source(source: QuerySource): Seq[bc.Instruction] = {
    source match {
      case SingleTable(id, _, _) => Seq(bc.Instruction(S.V), bc.Instruction(S.hasLabel, id))
    }

  }

//  def extract_value(v: Value): AnyVal = v match {
//    case RealValue(x) => x
//    case StringValue(x) => x
//    case BoolValue(x) => x
//  }

  def split_column_value(v1: Expression, v2: Expression) = {
    (v1, v2) match {
      case (ColumnRef(id, _, _), Val(v)) => (id, v)
      //      case (v: AtomicValue, ColumnRef(id, _, _)) => (id, extract_value(v))
      case _ => ???
    }
  }

  def compile_filter_expr(expr: PredicateExpression): bc.Instruction = expr match {
    case and: LogicalAnd => bc.Instruction(S.and, and.expr_list.map(x => bc.Bytecode(step = Seq(compile_filter_expr(x)))): _*)
    case or: LogicalOr => bc.Instruction(S.or, or.expr_list.map(x => bc.Bytecode(step = Seq(compile_filter_expr(x)))): _*)
    case LogicalNot(x) => ???
    case NumericalPredicate(op, arg1, arg2) => {
      val (col_id, v) = split_column_value(arg1, arg2)
      bc.Instruction(S.has, col_id, numeric_predicate_op(op, v))
    }
    case StringPredicate(op, arg1, arg2) => {
      val (col_id, v: String) = split_column_value(arg1, arg2)
      bc.Instruction(S.has, col_id, text_predicate_op(op, v))
    }
    case ContainmentPredicate(op, ColumnRef(col_id, _, _), arg2) => {
      bc.Instruction(S.has, col_id, containment_predicate_op(op, arg2.map(_.v)))
    }
  }

  def numeric_predicate_op[V](op: NumericalPredicateOp.Value, value: V): bc.Predicate[_] = op match {
    case NumericalPredicateOp.Equal => bc.Predicate.eq_(value)
    case NumericalPredicateOp.NotEqual => bc.Predicate.neq(value)
    case NumericalPredicateOp.< => bc.Predicate.lt(value)
    case NumericalPredicateOp.<= => bc.Predicate.lte(value)
    case NumericalPredicateOp.> => bc.Predicate.gt(value)
    case NumericalPredicateOp.>= => bc.Predicate.gte(value)
    case _ => throw new IllegalArgumentException(f"Unknown numerical predicate operation $op")
  }

  def text_predicate_op(op: StringPredicateOp.Value, value: String): bc.Predicate[_] = op match {
    case StringPredicateOp.Equal => bc.Predicate.eq_(value)
    case StringPredicateOp.NotEqual => bc.Predicate.neq(value)
    case StringPredicateOp.Containing => bc.Predicate.containing(value)
    case StringPredicateOp.NotContaining => bc.Predicate.notContaining(value)
    case StringPredicateOp.StartingWith => bc.Predicate.startingWith(value)
    case StringPredicateOp.EndingWith => bc.Predicate.endingWith(value)
    case _ => throw new IllegalArgumentException(f"Unknown text predicate operation $op")
  }

  def containment_predicate_op[V](op: ContainmentPredicateOp.Value, value: Seq[V]): bc.Predicate[_] = op match {
    case ContainmentPredicateOp.Within => bc.Predicate.within(value)
    case ContainmentPredicateOp.Without => bc.Predicate.without(value)
    case _ => throw new IllegalArgumentException(f"Unknown containment predicate operation $op")
  }

  def test1() = {
    val q = BasicQuery(
      SingleTable("PatientRecord", None, None),
      Some(QueryFilter(LogicalOr(
        NumericalPredicate(NumericalPredicateOp.<, ColumnRef("age"), Val(35.0)),
        StringPredicate(StringPredicateOp.Equal, ColumnRef("gender"), Val("жена"))
      ))),
      Some(QuerySort(ColumnSort(ColumnRef("age"), false))),
      Some(QueryRange(0, 100))
    )
    val g = compile(q)
    println(q)
    g
  }
}

//object ttt {
//  Bytecode(
//    List(),
//    List(
//      Instruction(V, List()),
//      Instruction(hasLabel, ArraySeq(PatientRecord)),
//      Instruction(filter, ArraySeq(
//        Bytecode(List(), List(
//          Instruction(and, ArraySeq(
//            Bytecode(List(), List(
//              Instruction(has, ArraySeq(age, Predicate(lt, 35.0))))),
//            Bytecode(List(), List(
//              Instruction(has, ArraySeq(gender, Predicate(eq, жена))))))))))),
//      Instruction(valueMap, List())))
//  Bytecode(
//    List(),
//    List(
//      Instruction(V, List()),
//      Instruction(hasLabel, ArraySeq(PatientRecord)),
//      Instruction(filter, ArraySeq(
//        Bytecode(List(), List(
//          Instruction(and, ArraySeq(
//            Bytecode(List(), List(
//              Instruction(has, ArraySeq(age, Predicate(lt, 35.0))),
//              Instruction(has, ArraySeq(gender, Predicate(eq, жена))))))))))),
//      Instruction(valueMap, List())))
//
//}