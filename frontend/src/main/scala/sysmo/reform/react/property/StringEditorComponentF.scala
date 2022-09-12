package sysmo.reform.react.property

import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.runtime.FLocal
import sysmo.reform.shared.sources.property.Dispatcher

class StringEditorComponentF[F[+_]]
(implicit val f2c: F2Callback[F])
  extends EncodedTextualEditor[F] {
  import Value.implicits._
  case class Props(id: ObjectId, value: Value, dispatcher: Dispatcher[F]) extends PropsBase

  override def format(value: Value): String = value.get[String].getOrElse("")
  override def parse(s: String): FLocal[Value] = Right(Value(s))

  override val display_name: String = "StringEditor"

  def apply(id: ObjectId, value: Value, dispatcher: Dispatcher[F]): Unmounted =
    component(Props(id, value, dispatcher))
}
