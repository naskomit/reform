package sysmo.reform.shared.util

import cats.MonadThrow

object MonadTesting {
  object implicits {
    implicit class MT_testing[F[_]](x: F[_])(implicit mt: MonadThrow[F]) {
      def detect_error(): Unit = {
        mt.onError(x)(e => throw e)
      }

      def error[A](f: Throwable => A): Unit = {
        mt.onError(x)(e => mt.pure(f(e)))
      }

    }

  }
}
