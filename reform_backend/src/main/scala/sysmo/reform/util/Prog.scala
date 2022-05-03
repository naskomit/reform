package sysmo.reform.util

import cats.syntax.{FlatMapSyntax, MonadErrorSyntax}

import scala.util.{Failure, Success, Try}

object Prog extends FlatMapSyntax with MonadErrorSyntax {
  type Res[+A] = Either[Throwable, A]
  implicit class ResMethods[A](res: Res[A]) {
    def info(logger: FuncLogger, msg: String): Res[A] = res.attemptTap (_ =>
      logger.info(msg)
    )

    /** Convert back to exception throwing class */
    def get: A = res match {
      case Right(a) => a
      case Left(err) => throw err
    }
    def unit: Res[Unit] = res.map(_ => ())

  }

  def ok[A](x: A): Res[A] = Right[Throwable, A](x)
  def error[A](x: Throwable): Res[A] = Left[Throwable, A](x)

  case class ErrorList(exceptions: Throwable*) extends Exception {

  }

  def collect_errors(r: Res[_]*): ErrorList = {
    val errors = r.collect {
      case Left(x) => x
    }
    ErrorList(errors: _*)
  }

  def all[A1, A2](r1: Res[A1], r2: Res[A2]): Res[(A1, A2)] = {
    (r1, r2) match {
      case (Right(v1), Right(v2)) => Right((v1, v2))
      case _ => Left(collect_errors(r1, r2))
    }
  }

  def all[A1, A2, A3](r1: Res[A1], r2: Res[A2], r3: Res[A3]): Res[(A1, A2, A3)] = {
    (r1, r2, r3) match {
      case (Right(v1), Right(v2), Right(v3)) => Right((v1, v2, v3))
      case _ => Left(collect_errors(r1, r2, r3))
    }
  }

  implicit class TryMethods[A](x: Try[A]) {
    def to_res: Res[A] = x match {
      case Failure(err) => Prog.error[A](err)
      case Success(res) => Prog.ok[A](res)
    }
  }

}


