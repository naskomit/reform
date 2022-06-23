package sysmo.reform.components.forms.editors

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ReactEventFromInput, ScalaComponent}
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.util.LabeledValue

object BooleanEditorComponent extends AbstractEditor[FB.BooleanField] {
  case class State()

  def format(v: FieldValueType): Boolean = {
    v match {
      case FR.SomeValue(LabeledValue(true, _)) => true
      case _ => false
    }

  }

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val field_prototype = p.obj.prototype
      <.div(
        ^.onClick ==> on_click(p),
        <.br(),
        <.div(^.className := "checkbox checkbox-inline",
          <.input(^.`type` := "checkbox", ^.autoFocus := false,
          ^.checked := format(p.value))
        )
      )
    }

//    def on_value_change(p: Props)(event: ReactEventFromInput): Callback = Callback {
////      println(s"Checkbox value ${p.field_obj.id.id} changed ")
//      //      action_generator.dispatch(SetValue(SomeValue(LabeledValue(event.target.value))))
//    }

    def on_click(p: Props)(event: ReactEventFromInput): Callback = Callback {
      p.obj.runtime.dispatch(
        FR.SetFieldValue(p.obj.id, FR.SomeValue(LabeledValue(!format(p.value))))
      )
    }

  }


  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props]("BooleanEditor")
      .initialState(State())
      .renderBackend[Backend]
      .build

}
