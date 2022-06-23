package sysmo.reform.components.forms.editors

import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}

object IntegerEditorComponent extends EncodedTextualEditor[FB.IntegerField] {
  override val display_name: String = "IntegerEditor"

  override def parse(s: String): Option[Int] = s.toIntOption

  def format(value: FieldValueType): String = {
    value match {
      case FR.SomeValue(LabeledValue(v, _)) => v.toString
      case _ => ""
    }
  }

  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]

}
