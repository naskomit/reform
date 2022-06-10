package sysmo.reform.components.forms4.editors

import sysmo.reform.components.forms4.FormDataHandler
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.{form => F}

import scala.{math => M}

object FloatEditorComponent extends EncodedTextualEditor[Double] {

  override val display_name: String = "FloatEditor"
  override type EditorType = F.FloatEditor

  override def parse(s: String): Option[Double] = s.toDoubleOption

  override def format(value: FieldValueType): String = {
    value match {
      case F.SomeValue(LabeledValue(v, _)) => {
        if (M.abs(v) > 1e10 || M.abs(v) < 1e-10) {
          "%e".format(v)
        } else if (v - M.floor(v) < 1e-18) {
          "%.1f".format(v)
        }
        else v.toString
      }
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
