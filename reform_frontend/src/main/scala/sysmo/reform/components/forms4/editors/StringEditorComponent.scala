package sysmo.reform.components.forms4.editors

import sysmo.reform.components.forms4.FormDataHandler
import sysmo.reform.shared.data.{form4 => F}
import sysmo.reform.shared.util.LabeledValue

object StringEditorComponent extends EncodedTextualEditor[String] {
  override val display_name: String = "StringEditor"
  override type EditorType = F.StringEditor

  override def parse(s: String): Option[String] = Some(s)

  def format(value: FieldValueType): String = {
    value match {
      case F.SomeValue(LabeledValue(v, _)) => v
      case _ => ""
    }
  }

  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]


  def apply(editor: EditorType, data_handler: FormDataHandler): Unmounted = {
    component(Props(editor, data_handler))

  }

}
