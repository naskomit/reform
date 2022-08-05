//package sysmo.reform.data
//
//import monix.execution.Scheduler.Implicits.global
//import monix.execution.cancelables.SingleAssignCancelable
//import monix.execution.{Ack, Cancelable}
//import monix.reactive.{Observable, Observer, OverflowStrategy}
//import sysmo.reform.components.editors.SetValue
//import sysmo.reform.shared.data.form.{NoValue, Record, RecordMeta, ValueDependency}
//import sysmo.reform.util.log.Logging
//
//import scala.collection.mutable
//import scala.concurrent.Future
//import scala.util.{Failure, Success}
//
//class StreamingRecordManager[U <: Record](initial_value: U, meta: RecordMeta[U]) extends Logging {
//  type State = Record.ValueMap
//  var on_new_state: Option[Function1[State, Ack]] = None
//  var state: State = meta.value_map(initial_value)
//
//  // TODO What happens if second subscription???
//  val record_stream: Observable[State] = Observable.create(OverflowStrategy.Unbounded)
//    { sub => {
//      sub.onNext(state)
//      val c = SingleAssignCancelable()
//      on_new_state = Some((state: State) => {
//        sub.onNext(state).syncOnStopOrFailure(_ => c.cancel())
//      })
//      meta.option_provider.label_values(state).onComplete {
//        case Success(values) => on_new_state.get(values)
//        case Failure(err) => logger.error(err)
//      }
//      c := Cancelable(() => on_new_state = None)
//    }
//  }
//
//  // TODO This is only direct dependency; also do we always want NoValue?
//  private def resolve_field_dependencies(field_id: String): Unit = {
//    val unresolved = mutable.Set()
//    val affected = meta.field_dependencies.collect {
//      case x: ValueDependency if x.sources.contains(field_id) => x.sink
//    }
//    affected.foreach(id => state = state + (id -> NoValue))
//  }
//
//  val action_handler: Observer[RecordAction] = new Observer[RecordAction] {
//    override def onNext(action: RecordAction): Future[Ack] = {
//      println(f"StreamingRecordManager: $action")
//      on_new_state match {
//        case Some(f) => action match {
//          case UpdateField(form_id, field_id, update) => {
//            val new_value = update match {
//              case SetValue(v) => v
//            }
//            state = state + (field_id -> new_value)
//            resolve_field_dependencies(field_id)
//            logger.info(s"New state: ${state.toString()}")
//            f(state)
//          }
//        }
//
//        case None =>
//      }
//      Ack.Continue
//    }
//
//    override def onError(ex: Throwable): Unit = logger.error(ex)
//
//    override def onComplete(): Unit = println(s"StreamingRecordManager / onComplete")
//  }
//
//  def validate: Either[Map[String, Throwable], U] = meta.validate(state)
//}
//
//object StreamingRecordManager {
//  def apply[U <: Record](initial_value : U, meta: RecordMeta[U]): StreamingRecordManager[U] =
//    new StreamingRecordManager[U](initial_value, meta)
//}
//
