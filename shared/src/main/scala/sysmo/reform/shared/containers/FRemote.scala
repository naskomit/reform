package sysmo.reform.shared.containers

import cats.MonadThrow
import sysmo.reform.shared.util.Injector

import scala.concurrent.{ExecutionContext, Future}

trait FRemote[+T] extends RFContainer[T] {
  protected val v: FRemote.FRemote_Future[T]
  override def to_future: Future[T] = v
}

object FRemote {
  type FRemote_Future[+T] = Future[T]
  case class FRemoteImpl[+T](protected val v: Future[T]) extends FRemote[T]
  def apply[T](x: T): FRemote[T] = FRemoteImpl(Future.successful(x))
  def error[T](e: Throwable): FRemote[T] = FRemoteImpl(Future.failed(e))
  def from_future[T](f: Future[T]): FRemote[T] = FRemoteImpl(f)

  def mt: MonadThrow[FRemote] = new DerivedMonadThrow[FRemote, FRemote_Future] {
    protected implicit val ec: ExecutionContext = Injector.inject[ExecutionContext]
    override val mt_inner: MonadThrow[FRemote_Future] = MonadThrow[FRemote_Future]
    override def construct[A](i: FRemote_Future[A]): FRemote[A] = FRemoteImpl(i)
    override def extract[A](o: FRemote[A]): FRemote_Future[A] = o.v
  }
}
