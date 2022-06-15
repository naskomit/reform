package sysmo.reform.components.forms.editors

import sysmo.reform.components.forms.FormDataHandler
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.{form => F}

object IntegerEditorComponent extends EncodedTextualEditor[Int] {
  override val display_name: String = "IntegerEditor"
  override type EditorType = F.IntegerEditor

  override def parse(s: String): Option[Int] = s.toIntOption

  def format(value: FieldValueType): String = {
    value match {
      case F.SomeValue(LabeledValue(v, _)) => v.toString
      case _ => ""
    }
  }

  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]


  def apply(editor: EditorType, data_handler: FormDataHandler)(options: FormRenderingOptions): Unmounted = {
    component(Props(editor, data_handler, options))

  }

}
