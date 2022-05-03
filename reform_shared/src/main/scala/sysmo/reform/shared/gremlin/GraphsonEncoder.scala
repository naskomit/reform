package sysmo.reform.shared.gremlin

import sysmo.reform.shared.gremlin.bytecode.{Bytecode, Instruction, Predicate, TextP}
import sysmo.reform.shared.gremlin.{bytecode => bc}
import sysmo.reform.shared.util.CirceTransport

object GraphsonEncoder extends CirceTransport {
  import io.circe.syntax._

  def to_value(sv: Any): Json = {
    sv match {
      case x: String => x.asJson
      case x: Double => x.asJson
      case x: Int => int32(x)
      case x: Long => int64(x)
      case x: Boolean => x.asJson
      case x: Bytecode => bytecode(x)
      case x: Instruction => instruction(x)
      case x: Predicate[_] => predicate(x)
      case x: bc.Order.Value if bc.Order.has(x) => order(x)
      case x if x == null => null
    }
  }

  def instruction(o: Instruction): Json = {
    Json.arr(Seq[Json](o.operator.asJson) ++ o.arguments.map(to_value): _*)
  }

  def bytecode(o: Bytecode): Json = Json.obj(
      ("@type", "g:Bytecode".asJson),
      ("@value", Json.obj(
        ("step", Json.arr(o.step.map(instruction): _*))
      ))
  )

  def order(v: bc.Order.Value): Json = Json.obj(
    ("@type", "g:Order".asJson),
    ("@value", v.toString.asJson)
  )

  def predicate[U](o: Predicate[U]): Json = {
    val _type = if (TextP.has(o.biPredicate)) {
      "g:TextP"
    } else "g:P"
    Json.obj(
      ("@type", _type.asJson),
      ("@value", Json.obj(
        ("predicate", o.biPredicate.toString.asJson),
        ("value", to_value(o.value))
      ))

    )

}

  def int32(o: Int): Json = Json.obj(
    ("@type", "g:Int32".asJson),
    ("@value", o.asJson)
  )

  def int64(o: Long): Json = Json.obj(
    ("@type", "g:Int64".asJson),
    ("@value", o.asJson)
  )
}
