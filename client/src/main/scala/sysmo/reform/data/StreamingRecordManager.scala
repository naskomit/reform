package sysmo.reform.data

import monix.execution.Scheduler.Implicits.global
import monix.execution.cancelables.SingleAssignCancelable
import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, Observer, OverflowStrategy}
import sysmo.reform.components.editors.SetValue
import sysmo.reform.shared.chart.DistributionSettings
import sysmo.reform.shared.data.{OptionProvider, Record, RecordMeta, RecordWithMeta, SomeValue}

import scala.concurrent.Future

class StreamingRecordManager[U <: Record](initial_value: U, meta: RecordMeta[U]) {
  type State = Record.ValueMap
  var on_new_state: Option[Function1[State, Ack]] = None
  var state: State = meta.value_map(initial_value)

  val record_stream: Observable[State] = Observable.create(OverflowStrategy.Unbounded)
    { sub => {
      sub.onNext(state)
      val c = SingleAssignCancelable()
      on_new_state = Some((state: State) => {
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
          case UpdateField(form_id, field_id, update) => {
            val new_value = update match {
              case SetValue(v) => SomeValue(v)
            }
            state = state + (field_id -> new_value)
            f(state)
          }
        }

        case None =>
      }
      Ack.Continue
    }

    override def onError(ex: Throwable): Unit = println(s"StreamingRecordManager / onError")

    override def onComplete(): Unit = println(s"StreamingRecordManager / onComplete")
  }

  def validate: Either[Map[String, Throwable], U] = meta.validate(state)
}

object StreamingRecordManager {
  def apply[U <: Record](initial_value : U)(implicit meta_holder: RecordWithMeta[U]): StreamingRecordManager[U] =
    new StreamingRecordManager[U](initial_value, meta_holder._meta)
}

