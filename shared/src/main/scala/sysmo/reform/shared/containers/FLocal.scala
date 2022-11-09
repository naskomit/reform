package sysmo.reform.shared.containers

import cats.data.Ior
import cats.{Align, Applicative, Eval, Functor, MonadError, MonadThrow, Monoid, Now, Traverse}
import cats.implicits._
import cats.syntax.{EitherUtil, FlatMapSyntax, all}

import scala.annotation.tailrec
import scala.concurrent.Future



trait FLocal[+T] extends RFContainer[T] {
  protected val v: FLocal.FLocal_Either[T]
  override def to_future: Future[T] = v match {
    case Left(error) => Future.failed(error)
    case Right(value) => Future.successful(value)
  }
  def to_either: Either[Throwable, T] = v
  def to_option: Option[T] = v.toOption
  def to[F[+_]](implicit mt: MonadThrow[F]) = {
    v match {
      case Left(err) => mt.raiseError(err)
      case Right(value) => mt.pure(value)
    }
  }
  def get: T = v match {
    case Left(error) => throw(error)
    case Right(value) => value
  }
}

object FLocal {
  type FLocal_Either[+T] = Either[Throwable, T]

  case class FLocalImpl[+T](protected val v: Either[Throwable, T]) extends FLocal[T]
  def apply[T](x: T): FLocal[T] = FLocalImpl(Right(x))
  def error[T](e: Throwable): FLocal[T] = FLocalImpl(Left(e))
  def from_option[T](x: Option[T]): FLocal[T] = x match {
    case Some(value) => apply(value)
    case None => error(new IllegalArgumentException("No value"))
  }

  def to_flocal_iter[E <: Throwable, T](it: Iterator[Either[E, T]]): Iterator[FLocal[T]] = new Iterator[FLocal[T]] {
    override def hasNext: Boolean = it.hasNext
    override def next(): FLocal[T] = it.next() match {
      case Left(err) => error(err)
      case Right(value) => apply(value)
    }
  }

  def mt: MonadThrow[FLocal] = new DerivedMonadThrow[FLocal, FLocal_Either] {
    override val mt_inner: MonadThrow[FLocal_Either] = MonadThrow[FLocal_Either]
    override def construct[A](i: FLocal_Either[A]): FLocal[A] = FLocalImpl(i)
    override def extract[A](o: FLocal[A]): FLocal_Either[A] = o.v
  }
}
