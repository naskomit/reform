package sysmo.reform.effects

import cats.MonadThrow
import japgolly.scalajs.react.callback.{AsyncCallback, CallbackTo}
import org.scalajs.dom.console
import sysmo.reform.shared.runtime.FLocal

import scala.scalajs.js
import scala.scalajs.js.|

object implicits {
  trait F2Callback[F[+_]] {
    def sync[T](f: F[T]): CallbackTo[T]
    def sync_pure[T](t: T): CallbackTo[T]
    def async[T](f: F[T]): AsyncCallback[T]
    def async_pure[T](t: T): AsyncCallback[T]
  }
  implicit object FLocal2AsyncCallback extends F2Callback[FLocal] {
    def sync[T](f: FLocal[T]): CallbackTo[T] = {
      f match {
        case Left(error) => CallbackTo.throwException(error)
        case Right(value) => CallbackTo(value)
      }
    }

    def sync_pure[T](t: T): CallbackTo[T] = {
      val mt = MonadThrow[FLocal]
      sync(mt.pure(t))
    }

    def async[T](f: FLocal[T]): AsyncCallback[T] = {
      val mt = MonadThrow[FLocal]
      AsyncCallback.fromJsPromise(
        new js.Promise[T]((respond: js.Function1[T | js.Thenable[T], _], reject: js.Function1[Any, _]) => {
          mt.handleError(
            mt.map(f)(res => respond(res))
          ){e =>
            console.error(e)
            reject(e)
          }
        })
      )
    }

    def async_pure[T](t: T): AsyncCallback[T] = {
      val mt = MonadThrow[FLocal]
      async(mt.pure(t))
    }

  }
}
