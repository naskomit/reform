package sysmo.reform.components.forms3.editors

import sysmo.reform.components.ReactComponent
import japgolly.scalajs.react.{BackendScope, ReactEventFromInput, ScalaComponent}
import sysmo.reform.shared.data.{form3 => F}
import F.{FormData => FD}

trait AbstractEditor[VT] extends ReactComponent {
  type ValueType = VT
  type FieldValueType = FD.FieldValue[VT]

  trait Status
  case object Valid extends Status
  case class Error(msg: String, value: String) extends Status
  case class Warning(msg: String, value: String) extends Status
//  trait EditorProps {
//    val form_dispatcher: EditorAction.Dispatcher
//  }

//  override type Props  <: EditorProps

//  class EditorBackend($: BackendScope[Props, State]) {
////    def dispatch(action: EditorAction): Unit = action_dispatcher.dispatch(action)
//  }
//
//  override type Backend <: EditorBackend

}
