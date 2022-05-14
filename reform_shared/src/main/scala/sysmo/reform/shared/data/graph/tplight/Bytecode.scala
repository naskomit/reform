package sysmo.reform.shared.data.graph.tplight

case class Instruction(operator: String, arguments: Any*)

class Bytecode {
  val source_instructions: Seq[Instruction] = Seq()
  val step_instructions: Seq[Instruction] = Seq()
}
