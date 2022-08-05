package sysmo.reform.components.property
import japgolly.scalajs.react.component.Scala
import sysmo.reform.shared.field.Value
import sysmo.reform.shared.property.Property

object StringEditorComponent extends EncodedTextualEditor {
  override val display_name: String = "StringEditor"
  case class Props1[K1](property: Property[K1, String]) extends Props {
    type K = K1
    type V = String
    override def format(value: Value[V]): String = value.get_opt.getOrElse("").toString
    override def parse(s: String): Res[Value[V]] =
      Ok(Value.char(s))
  }

  def apply[K](property: Property[K, String]): Unmounted = {
    component(Props1(property))
  }
}
