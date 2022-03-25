package sysmo.reform.components.editors

import sysmo.reform.components.ReactAction

trait EditorAction extends ReactAction

case class ValueChanged[U](value : U) extends EditorAction
case class GotFocus() extends EditorAction
case class LostFocus() extends EditorAction