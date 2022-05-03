package sysmo.reform.components.actions

import japgolly.scalajs.react.Callback
import monix.execution.cancelables.SingleAssignCancelable
import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, Observer, OverflowStrategy}
import monix.execution.Scheduler.Implicits.global

class ActionStreamGenerator[U] {
  var dispatcher : Option[Function1[U, Ack]] = None

  val actions: Observable[U] = Observable.create(OverflowStrategy.Unbounded) {
    sub => {
      val c = SingleAssignCancelable()
      dispatcher = Some((action: U) => {
        sub.onNext(action).syncOnStopOrFailure(_ => c.cancel())
      })
      c := Cancelable(() => dispatcher = None)
    }
  }

  var subscription : Option[Cancelable] = None

  def start(s : Observer[U]) : Unit = {
    subscription match {
      case Some(_) => throw new IllegalStateException("Action stream already started")
      case None => subscription = Some(actions.subscribe(s))
    }
  }

  def stop() : Callback = {
    subscription match {
      case Some(subscr) => subscription = None
      case None => throw new IllegalStateException("Action stream already stopped")
    }
    Callback.empty
  }

  def dispatch(action : U) : Unit =
    dispatcher match {
      case Some(f) =>  f(action)
      case _ =>
    }
}

object ActionStreamGenerator {
  def apply[U]: ActionStreamGenerator[U] = new ActionStreamGenerator[U]()
}