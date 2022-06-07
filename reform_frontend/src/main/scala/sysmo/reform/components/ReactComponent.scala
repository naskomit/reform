package sysmo.reform.components

import japgolly.scalajs.react.callback.{Exports => cb_exports}
import japgolly.scalajs.react.component.Scala
import sysmo.reform.util.log.Logging

trait ReactAction

trait ReactComponent extends Logging with cb_exports {
  type Props
  type State
  type Backend
  type Unmounted = Scala.Unmounted[Props, State, Backend]

//  class BaseEffects($: BackendScope[Props, State]) {
////    val state_identity = (p: Props, s: State) => s
////    def with_state[A](props: Props)(f: (Props, State) => CallbackTo[A]) = {
////      var result: Option[CallbackTo[A]] = None
////      $.modState(state => {
////        result = f(props, state)
////        state
////      })
////      result
////    }
//  }

//  trait IBackend {
//    def handle_action(props: Props, state: State)(action: ReactAction): AsyncCallback[Unit]
//
//    def dispatch(props: Props, state: State)(action: ReactAction): AsyncCallback[Unit] = {
//      handle_action(props, state)(action)
//    }
//
//  }



//  protected def future_effect[A](fut: Future[A], on_success: A => AsyncCallback[Unit], on_error: Throwable => AsyncCallback[Unit]): AsyncCallback[Unit] = {
//    AsyncCallback.fromFuture(fut)
//      .flatMap(res => {
//        on_success(res)
//      }).handleError(err => {
//      logger.error(err.getMessage)
//      on_error(err)
//    })
//  }
//
//  protected def delay_future[A](fut: Future[A], interval: Int): Future[A] = {
//    import scala.scalajs.js.timers._
//    fut.flatMap(x => {
//      val prom = Promise[A]()
//      setTimeout(interval){
//        prom.success(x)
//      }
//      prom.future
//    })
//  }
}
