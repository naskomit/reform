package sysmo.reform.components.forms.editors

import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}

object StringEditorComponent extends EncodedTextualEditor[FB.StringField] {
  override val display_name: String = "StringEditor"

  override def parse(s: String): Option[String] = Some(s)

  def format(value: FieldValueType): String = {
    value match {
      case FR.SomeValue(LabeledValue(v, _)) => v
      case _ => ""
    }
  }

  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]


//  def apply(field_obj: FR.AtomicValue[FB.StringField], options: FormRenderingOptions): Unmounted = {
//    component(Props(field_obj, options))
//
//  }

}
