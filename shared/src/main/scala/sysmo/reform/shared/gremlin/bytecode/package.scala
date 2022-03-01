package sysmo.reform.shared.gremlin

package object bytecode {

  case class Instruction(operator: String, arguments: Any*)

  case class Bytecode(
     source: Seq[Instruction] = Seq(),
     step: Seq[Instruction])


  object Bytecode {
    import sysmo.reform.shared.gremlin.bytecode.{Predicate => P, Symbols => S}

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

  }
}
