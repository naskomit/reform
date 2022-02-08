package sysmo.coviddata.components.editors

trait EditorAction

case class ValueChanged[U](value : U) extends EditorAction
case class GotFocus() extends EditorAction
case class LostFocus() extends EditorAction