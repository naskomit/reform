package sysmo.reform.shared.gremlin

import ujson.{Arr, Obj, Value}

object GraphsonEncoder {
  def to_value(sv: Any): Value = {
    sv match {
      case x: String => x
      case x: Double => x
      case x: Int => int32(x)
      case x: Boolean => x
      case x: Bytecode => bytecode(x)
      case x: Instruction => instruction(x)
      case x: Predicate[_] => predicate(x)
      case x: TextPredicate => text_predicate(x)
      case x if x == null => null
    }
  }

  def instruction(o: Instruction): Value = {
    Arr(Seq[Value](o.operator) ++ o.arguments.map(to_value): _*)
  }

  def bytecode(o: Bytecode): Value = Obj(
      ("@type", "g:Bytecode"),
      ("@value", Obj(
        ("step", Arr(o.step.map(instruction): _*))
      ))
  )

  def predicate[U](o: Predicate[U]): Value = {
    Obj(
      ("@type", "g:P"),
      ("@value", Obj(
        ("predicate", o.biPredicate.toString),
        ("value", to_value(o.value))
      ))
    )
  }

  def text_predicate(o: TextPredicate): Value = ???

  def int32(o: Int): Obj = Obj(
    ("@type", "g:Int32"),
    ("@value", o)
  )
}
