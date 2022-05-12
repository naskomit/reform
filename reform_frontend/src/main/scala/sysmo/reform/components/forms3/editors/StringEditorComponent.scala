package sysmo.reform.components.forms3.editors

import sysmo.reform.shared.data.{form3 => F}
import F.{FormData => FD}

import sysmo.reform.shared.util.LabeledValue
import scala.{math => M}

object StringEditorComponent extends EncodedTextualEditor[String] {
  override val display_name: String = "StringEditor"
  override type EditorType = F.StringEditor

  override def parse(s: String): Option[String] = Some(s)

  def format(value: FieldValueType): String = {
    value match {
      case FD.SomeValue(LabeledValue(v, _)) => v
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

//import japgolly.scalajs.react.vdom.html_<^._
//import japgolly.scalajs.react.{BackendScope, ReactEventFromInput, ScalaComponent}
//import sysmo.reform.shared.data.form3.{StringEditor, FormData => FD}
//import sysmo.reform.shared.util.LabeledValue
//
//object StringEditorComponent extends AbstractEditor[String] {
//  case class Props(editor: StringEditor, value: FieldValueType, form_dispatcher: EditorAction.Dispatcher)
//  case class State()
//
//  final class Backend($: BackendScope[Props, State]) {
//    def render(p: Props, s: State): VdomElement = {
//      val value = p.value match {
//        case FD.SomeValue(v) => v.value
//        case _ => ""
//      }
//      <.div(^.className := "form-group", ^.key := p.editor.name, ^.id := p.editor.path.toString,
//        <.label(p.editor.make_label),
//        <.input(^.`type` := "text", ^.className := "form-control", ^.autoFocus := false,
//          ^.value := value,
//          ^.onChange ==> on_value_change(p),
//          ^.onFocus --> on_focus, ^.onBlur --> on_blur
//        ))
//    }
//
//    def on_value_change(p: Props)(event: ReactEventFromInput): Callback = Callback {
//      val value = event.target.value match {
//        case "" => FD.NoValue
//        case x => FD.SomeValue(LabeledValue(x))
//      }
//      p.form_dispatcher.dispatch(SetFieldValue(p.editor.path, value))
//    }
//
//    def on_focus = Callback {
//    }
//
//    def on_blur = Callback {
//    }
//
//  }
//
//
//  //  // TODO
//  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
//  //  implicit val props_reuse = Reusability.by((_ : Props).value)
//  //  implicit val state_reuse = Reusability.derive[State]
//
//  val component = ScalaComponent.builder[Props]("StringEditorComponent")
//    .initialState(State())
//    .renderBackend[Backend]
//    .build
//
//  def apply(editor: StringEditor, value: FieldValueType, form_dispatcher: EditorAction.Dispatcher): Unmounted = {
//    component(Props(editor, value, form_dispatcher: EditorAction.Dispatcher))
//  }
//
//}
