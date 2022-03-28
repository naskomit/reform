package sysmo.reform.components.editors

import sysmo.reform.components.ReactAction

trait EditorAction extends ReactAction

trait UpdateValue extends EditorAction
case class SetValue[U](value : U) extends UpdateValue

case class GotFocus() extends EditorAction
case class LostFocus() extends EditorAction