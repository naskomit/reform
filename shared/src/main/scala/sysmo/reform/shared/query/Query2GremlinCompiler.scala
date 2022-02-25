package sysmo.reform.shared.query

import sysmo.reform.shared.{gremlin => Gr}
import sysmo.reform.shared.gremlin.GraphTraversal.{Symbols => S}

import scala.collection.mutable

object Query2GremlinCompiler {
  def compile(q: Query): Gr.Bytecode = {
    q match {
      case BasicQuery(source, filter, sort, range) => {

        // Create bytecode step buffer and add source
        val steps = mutable.ArrayBuffer[Gr.Instruction](
          compile_source(source): _*
        )
        // Add filters
        filter match {
          case Some(flt) => steps += Gr.Instruction(
            S.filter, Gr.Bytecode(step = Seq(compile_filter_expr(flt.expr)))
          )

          case None =>
        }
        // Convert to value map
        steps += Gr.Instruction(S.valueMap)

        // Return more detailed output
        // steps += Instruction(S.with_, Seq("~tinkerpop.valueMap.tokens", 15))

        // Return bytecode
        Gr.Bytecode(step = steps.toSeq)
      }

      case _ => throw new IllegalArgumentException(f"Cannot compile query $q to Gremlin")
    }
  }

  def compile_source(source: QuerySource): Seq[Gr.Instruction] = {
    source match {
      case SingleTable(id, _, _) => Seq(Gr.Instruction(S.V), Gr.Instruction(S.hasLabel, id))
    }

  }

  def extract_value(v: AtomicValue): Any = v match {
    case RealValue(x) => x
    case StringValue(x) => x
    case BoolValue(x) => x
  }

  def split_column_value(v1: Expression, v2: Expression) = {
    (v1, v2) match {
      case (ColumnRef(id, _, _), v: AtomicValue) => (id, extract_value(v))
      //      case (v: AtomicValue, ColumnRef(id, _, _)) => (id, extract_value(v))
      case _ => ???
    }
  }

  def compile_filter_expr(expr: PredicateExpression): Gr.Instruction = expr match {
    case LogicalAnd(expr_list) => Gr.Instruction(S.and, expr_list.map(x => Gr.Bytecode(step = Seq(compile_filter_expr(x)))): _*)
    case LogicalOr(expr_list) => Gr.Instruction(S.or, expr_list.map(x => Gr.Bytecode(step = Seq(compile_filter_expr(x)))): _*)
    case LogicalNot(x) => ???
    case NumericalPredicate(op, arg1, arg2) => {
      val (col_id, v) = split_column_value(arg1, arg2)
      Gr.Instruction(S.has, col_id, numeric_predicate_op(op, v))
    }
    case StringPredicate(op, arg1, arg2) => {
      val (col_id, v) = split_column_value(arg1, arg2)
      Gr.Instruction(S.has, col_id, text_predicate_op(op, v))
    }
    case ContainmentPredicate(op, ColumnRef(col_id, _, _), arg2) => {
      Gr.Instruction(S.has, col_id, containment_predicate_op(op, arg2.map(extract_value)))
    }
  }

  def numeric_predicate_op[V](op: NumericalPredicateOp.Value, value: V): Gr.Predicate[_] = op match {
    case NumericalPredicateOp.Equal => Gr.Predicate.eq_(value)
    case NumericalPredicateOp.NotEqual => Gr.Predicate.neq(value)
    case NumericalPredicateOp.< => Gr.Predicate.lt(value)
    case NumericalPredicateOp.<= => Gr.Predicate.lte(value)
    case NumericalPredicateOp.> => Gr.Predicate.gt(value)
    case NumericalPredicateOp.>= => Gr.Predicate.gte(value)
    case _ => throw new IllegalArgumentException(f"Unknown numerical predicate operation $op")
  }

  def text_predicate_op[V](op: StringPredicateOp.Value, value: V): Gr.Predicate[_] = op match {
    case StringPredicateOp.Equal => Gr.Predicate.eq_(value)
    case StringPredicateOp.NotEqual => Gr.Predicate.neq(value)
    case _ => throw new IllegalArgumentException(f"Unknown text predicate operation $op")
  }

  def containment_predicate_op[V](op: ContainmentPredicateOp.Value, value: Seq[V]): Gr.Predicate[_] = op match {
    case ContainmentPredicateOp.Within => Gr.Predicate.within(value)
    case ContainmentPredicateOp.Without => Gr.Predicate.without(value)
    case _ => throw new IllegalArgumentException(f"Unknown containment predicate operation $op")
  }

  def test1() = {
    val q = BasicQuery(
      SingleTable("PatientRecord", None, None),
      Some(QueryFilter(LogicalOr(Seq(
        NumericalPredicate(NumericalPredicateOp.<, ColumnRef("age"), RealValue(35.0)),
        StringPredicate(StringPredicateOp.Equal, ColumnRef("gender"), StringValue("жена"))
      )))),
      Some(QuerySort(Vector(ColumnSort(ColumnRef("age"), false)))),
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