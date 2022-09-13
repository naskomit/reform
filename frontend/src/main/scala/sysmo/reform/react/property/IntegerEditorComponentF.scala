package sysmo.reform.react.property

import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.runtime.FLocal

class IntegerEditorComponentF[F[+_]]
(implicit val f2c: F2Callback[F])
  extends EncodedTextualEditor[F] {
  import Value.implicits._
  case class Props(id: ObjectId, value: Value, dispatcher: Dispatcher[F]) extends PropsBase

  override def format(value: Value): String = value.get[String].getOrElse("")
  override def parse(s: String): FLocal[Value] = s.toIntOption match {
      case Some(x) => Right(Value(x))
      case None => Left(new IllegalArgumentException(s"Cannot convert ${s} to integer"))
    }

  override val display_name: String = "IntegerEditor"

  def apply(id: ObjectId, value: Value, dispatcher: Dispatcher[F]): Unmounted =
    component(Props(id, value, dispatcher))
}
