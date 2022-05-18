package sysmo.reform.shared.gremlin.tplight

case class Instruction(operator: String, arguments: Any*)

case class Bytecode(source_instructions: Seq[Instruction], step_instructions: Seq[Instruction]) {
  def add_step(name: String, arguments: Any*): Bytecode = {
    new Bytecode(source_instructions, step_instructions :+ Instruction(name, arguments: _*))
  }
}

object Bytecode {
  object Empty extends Bytecode(Seq(), Seq())
  def apply() = new Bytecode(Seq(), Seq())
}