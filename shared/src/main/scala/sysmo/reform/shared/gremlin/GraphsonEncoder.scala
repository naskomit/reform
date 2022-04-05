package sysmo.reform.shared.gremlin

import sysmo.reform.shared.gremlin.bytecode.{Bytecode, Instruction, Predicate, TextP}
import sysmo.reform.shared.gremlin.{bytecode => bc}
import ujson.{Arr, Obj, Value}

object GraphsonEncoder {
  def to_value(sv: Any): Value = {
    sv match {
      case x: String => x
      case x: Double => x
      case x: Int => int32(x)
      case x: Long => int64(x)
      case x: Boolean => x
      case x: Bytecode => bytecode(x)
      case x: Instruction => instruction(x)
      case x: Predicate[_] => predicate(x)
      case x: bc.Order.Value if bc.Order.has(x) => order(x)
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

  def order(v: bc.Order.Value): Value = Obj(
    ("@type", "g:Order"),
    ("@value", v.toString)
  )

  def predicate[U](o: Predicate[U]): Value = {
    val _type = if (TextP.has(o.biPredicate)) {
      "g:TextP"
    } else "g:P"
    Obj(
      ("@type", _type),
      ("@value", Obj(
        ("predicate", o.biPredicate.toString),
        ("value", to_value(o.value))
      ))

    )

}

  def int32(o: Int): Obj = Obj(
    ("@type", "g:Int32"),
    ("@value", o)
  )

  def int64(o: Long): Obj = Obj(
    ("@type", "g:Int64"),
    ("@value", o)
  )
}
