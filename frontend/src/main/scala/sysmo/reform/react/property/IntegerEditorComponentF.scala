package sysmo.reform.react.property

import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.runtime.RecordFieldInstance
import sysmo.reform.shared.containers.FLocal

class IntegerEditorComponentF[F[+_]]
(implicit val f2c: F2Callback[F])
  extends EncodedTextualEditor[F] {
  import Value.implicits._
  case class Props(id: ObjectId, field: RecordFieldInstance, dispatcher: Dispatcher[F]) extends PropsBase

  override def format(value: Value): String = value.get[String].getOrElse("")
  override def parse(s: String): FLocal[Value] = s.toIntOption match {
      case Some(x) => FLocal(Value(x))
      case None => FLocal.error(new IllegalArgumentException(s"Cannot convert ${s} to integer"))
    }

  override val display_name: String = "IntegerEditor"

  def apply(id: ObjectId, field: RecordFieldInstance, dispatcher: Dispatcher[F]): Unmounted =
    component(Props(id, field, dispatcher))
}
