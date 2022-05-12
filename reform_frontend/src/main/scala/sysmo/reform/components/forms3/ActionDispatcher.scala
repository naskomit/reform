package sysmo.reform.components.forms3

import monix.execution.{Ack, Cancelable}
import monix.execution.cancelables.SingleAssignCancelable
import monix.reactive.observers.Subscriber
import monix.reactive.{Observable, Observer, OverflowStrategy}

import scala.concurrent.Future
import sysmo.reform.util.log.Logging
import monix.execution.Scheduler.Implicits.global

trait ActionDispatcher[A] extends Logging {
  protected val c = SingleAssignCancelable()
  protected var _sub: Option[Subscriber.Sync[A]] = None
  def handle_action(action: A): Unit
  def handle_error(error: Throwable): Unit = {}

  protected val receiver = new Observer[A] {
    override def onNext(action: A): Future[Ack] = {
      logger.info(f"ActionDispatcher: $action")
      handle_action(action)
      Ack.Continue
    }
    override def onError(ex: Throwable): Unit = {
      logger.error(ex)
      handle_error(ex)
    }

    override def onComplete(): Unit = logger.info(s"ActionDispatcher completed")
  }

  protected val sender: Observable[A] = Observable.create(OverflowStrategy.Unbounded) {
    sub => {
      logger.info(f"ActionDispatcher subscribing")
      _sub = Some(sub)
      c := Cancelable(() => {
        _sub = None
      })
    }
  }

  protected val subscription = sender.subscribe(receiver)

  def dispatch(action: A): Unit = {
    _sub.get.onNext(action).syncOnStopOrFailure(e => {
      e match {
        case Some(x) => logger.error(x)
        case _ =>
      }
      c.cancel()
    })
  }

  def release(): Unit = {
    subscription.cancel()
  }

}