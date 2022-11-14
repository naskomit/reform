package sysmo.reform.shared.expr

import cats.MonadThrow
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.util.MonadicIterator

trait Context[_F[+_]] {
  type F[+X] = _F[X]
  type V = Value
  val mt: MonadThrow[F]
  def get(key: String): F[V]
  def get(path: Seq[String]): F[V] = get(path.mkString("."))
  def iterator: MonadicIterator[F, (String, V)]
  def updated(key: String, value: V):  Context[F]
  def removed(key: String): Context[F]
}

object Context {
  import scala.concurrent.{ExecutionContext, Future}
  import cats.instances.future._
  class MapBasedFutureContext(base: Map[String, Value])(implicit ec: ExecutionContext) extends Context[Future] {
    override val mt: MonadThrow[Future] = MonadThrow.apply[Future]
    override def get(key: String): F[V] = base.get(key) match {
      case Some(x) => Future.successful(x)
      case None => Future.successful(Value.empty)
//      case None => Future.failed(new NoSuchElementException(s"Element $key not found"))
    }
    override def iterator: MonadicIterator[F, (String, V)] = MonadicIterator.from_iterator(base.iterator)
    override def updated(key: String, value: V):  Context[F] =
      new MapBasedFutureContext(base.updated(key, value))
    override def removed(key: String): Context[F] =
      new MapBasedFutureContext(base.removed(key))
  }

  def fromMap_Future(base: Map[String, Value])(implicit ec: ExecutionContext): Context[Future] =
    new MapBasedFutureContext(base)
  def fromMap_Future()(implicit ec: ExecutionContext): Context[Future] =
    fromMap_Future(Map[String, Value]())

  import cats.instances.either._
  type EitherRes[+T] = Either[Throwable, T]
  class MapBasedTryContext(base: Map[String, Value]) extends Context[EitherRes] {
    override val mt: MonadThrow[EitherRes] = MonadThrow.apply[EitherRes]
    override def get(key: String): F[V] = base.get(key) match {
      case Some(x) => Right(x)
      case None => Right(Value.empty)
    }
    override def iterator: MonadicIterator[F, (String, V)] = MonadicIterator.from_iterator(base.iterator)
    override def updated(key: String, value: V):  Context[F] =
      new MapBasedTryContext(base.updated(key, value))
    override def removed(key: String): Context[F] =
      new MapBasedTryContext(base.removed(key))
  }

  def fromMap_Either(base: Map[String, Value]): Context[EitherRes] =
    new MapBasedTryContext(base)
  def fromMap_Either()(implicit ec: ExecutionContext): Context[EitherRes] =
    fromMap_Either(Map())

}


