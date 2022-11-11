package sysmo.reform.effects

import cats.MonadThrow
import cats.syntax.all._
import japgolly.scalajs.react.callback.{AsyncCallback, CallbackTo}
import org.scalajs.dom.console
import sysmo.reform.shared.containers.{FLocal, FRemote}
import sysmo.reform.shared.containers.implicits._
import sysmo.reform.shared.logging.Logging

import scala.scalajs.js
import scala.scalajs.js.|

object implicits {

  trait F2Callback[F[+_]] extends Logging {
    implicit val mt: MonadThrow[F]

    def async[T](f: F[T]): AsyncCallback[T] =
      AsyncCallback.fromJsPromise(
        new js.Promise[T]((respond: js.Function1[T | js.Thenable[T], _], reject: js.Function1[Any, _]) => {
          mt.handleError(
            mt.map(f)(res => respond(res))
          ) { e =>
            logger.error(e)
            reject(e)
          }
        })
      )

    def async_pure[T](t: T): AsyncCallback[T] =
      async(mt.pure(t))
  }


  trait F2CallbackSync[F[+_]] extends F2Callback[F] {
    def sync[T](f: F[T]): CallbackTo[T]
    def sync_pure[T](t: T): CallbackTo[T]

  }

  implicit object FLocal2AsyncCallback extends F2CallbackSync[FLocal] {
    val mt = MonadThrow[FLocal]

    override def sync[T](f: FLocal[T]): CallbackTo[T] = {
      f.to_either match {
        case Left(error) => CallbackTo.throwException(error)
        case Right(value) => CallbackTo(value)
      }
    }

    override def sync_pure[T](t: T): CallbackTo[T] = {
      sync(mt.pure(t))
    }

  }

  implicit object FRemote2AsyncCallback extends F2Callback[FRemote] {
    val mt: MonadThrow[FRemote] = MonadThrow[FRemote]
  }
}