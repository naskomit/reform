package sysmo.reform.shared.gremlin

import sysmo.reform.shared.gremlin.GraphTraversal.{Symbols => S}
import sysmo.reform.shared.gremlin.{Predicate => P}
import upickle.default._

case class Instruction(operator: String, arguments: Any*)

case class Bytecode(
  source: Seq[Instruction] = Seq(),
  step: Seq[Instruction]
)




object Bytecode {
  val prog1 = Bytecode(step = Seq(
    Instruction(S.V),
    Instruction(S.hasLabel, "PatientRecord"),
    Instruction(S.filter, Bytecode(step = Seq(
      Instruction(S.and,
        Bytecode(step = Seq(
          Instruction(S.has, "age", P.lt(35.0))
        )),
        Bytecode(step = Seq(
          Instruction(S.has, "gender", P.eq_("жена"))
        ))
      ))
    )),
    Instruction(S.valueMap),
//    Instruction(S.with_, "~tinkerpop.valueMap.tokens", 15),
  ))

  def test_serialization(prog: Bytecode): String = {
    write(GraphsonEncoder.to_value(prog))
  }

}