package sysmo.reform.components.forms3.editors

import sysmo.reform.shared.data.{form3 => F}
import F.{FormData => FD}

import sysmo.reform.shared.util.LabeledValue
import scala.{math => M}

object IntegerEditorComponent extends EncodedTextualEditor[Int] {
  override val display_name: String = "IntegerEditor"
  override type EditorType = F.IntegerEditor

  override def parse(s: String): Option[Int] = s.toIntOption

  def format(value: FieldValueType): String = {
    value match {
      case FD.SomeValue(LabeledValue(v, _)) => v.toString
      case _ => ""
    }
  }

  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]


  def apply(editor: EditorType, value: FieldValueType, form_dispatcher: EditorAction.Dispatcher): Unmounted = {
    component(Props(editor, value, form_dispatcher))

  }

}
