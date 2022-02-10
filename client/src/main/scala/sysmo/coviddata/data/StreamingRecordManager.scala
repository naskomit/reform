package sysmo.coviddata.data

import monix.execution.{Ack, Cancelable}
import monix.execution.cancelables.SingleAssignCancelable
import monix.reactive.{Observable, Observer, OverflowStrategy}
import monix.execution.Scheduler.Implicits.global
import sysmo.reform.shared.data.{Record, RecordMeta, RecordWithMeta}

import scala.concurrent.Future

class StreamingRecordManager[U <: Record](initial_value: U, meta: RecordMeta[U]) {
  var on_new_state: Option[Function1[U, Ack]] = None
  var state: U = initial_value
//  val meta = meta_holder._meta

  val record_stream: Observable[U] = Observable.create(OverflowStrategy.Unbounded)
    { sub => {
      sub.onNext(state)
      val c = SingleAssignCancelable()
      on_new_state = Some((state: U) => {
        sub.onNext(state).syncOnStopOrFailure(_ => c.cancel())
      })
      c := Cancelable(() => on_new_state = None)
    }
  }
  val action_handler: Observer[RecordAction] = new Observer[RecordAction] {
    override def onNext(action: RecordAction): Future[Ack] = {
      println(f"StreamingRecordManager: $action")
      on_new_state match {
        case Some(f) => action match {
          case UpdateField(form_id, field_id, value) => {
            state = meta.update_value(state, meta.field_key(field_id), value)
            f(state)
          }
        }

        case None =>
      }
      Ack.Continue
    }

    override def onError(ex: Throwable): Unit = ???

    override def onComplete(): Unit = ???
  }
}

object StreamingRecordManager {
  def apply[U <: Record](value : U)(implicit meta_holder: RecordWithMeta[U]): StreamingRecordManager[U] =
    new StreamingRecordManager[U](value, meta_holder._meta)
}

