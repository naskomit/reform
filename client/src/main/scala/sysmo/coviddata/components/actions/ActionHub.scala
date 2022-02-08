package sysmo.coviddata.components.actions

import monix.execution.cancelables.SingleAssignCancelable
import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, Observer, OverflowStrategy}
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future

class ActionHub[U, V](inputs: Seq[String], converter: (String, U) => Seq[V]) {
  val in_observers: Map[String, Observer[U]] = inputs.map(id => {
    val observer = new Observer[U] {
      override def onNext(action: U): Future[Ack] = {
        println(f"ActionHub[$id] $action")
        dispatcher match {
          case Some(f) => {
            f(converter(id, action))
            Ack.Continue
          }
          case None => Ack.Continue
        }
      }

      override def onError(ex: Throwable): Unit = ???

      override def onComplete(): Unit = ???
    }
    (id, observer)
  }).toMap

  private var dispatcher : Option[Function1[Seq[V], Ack]] = None

  val out: Observable[V] = Observable.create(OverflowStrategy.Unbounded) {
    sub => {
      val c = SingleAssignCancelable()
      dispatcher = Some((actions: Seq[V]) => {
        sub.onNextAll(actions).syncOnStopOrFailure(_ => c.cancel())
        Ack.Continue
      })
      c := Cancelable(() => dispatcher = None)
    }
  }

}

object ActionHub {
  def apply[U, V](inputs: Seq[String], converter: (String, U) => Seq[V]): ActionHub[U, V] =
    new ActionHub[U, V](inputs, converter)
}


