package sysmo.reform.fsm

import monix.reactive.Observable

trait Action

trait FiniteStateMachine[State] {
  type ActionType <: Action
  type StateType = State
  var state: StateType
  val children: Seq[FiniteStateMachine[_]]
  def dispatch(action: ActionType): Unit = {

  }

}


