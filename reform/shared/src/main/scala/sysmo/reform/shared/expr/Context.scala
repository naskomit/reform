package sysmo.reform.shared.expr

import cats.MonadThrow
import sysmo.reform.shared.util.MonadicIterator

trait Context[_F[+_], +V] {
  type F[+X] = _F[X]
  val mt: MonadThrow[F]
  def get(key: String): F[V]
  def iterator: MonadicIterator[F, (String, V)]
  def updated[V1 >: V](key: String, value: V1):  Context[F, V1]
  def removed(key: String): Context[F, V]
}

object Context {
  import scala.concurrent.{ExecutionContext, Future}
  import cats.instances.future._
  class MapBasedFutureContext[V](base: Map[String, V])(implicit ec: ExecutionContext) extends Context[Future, V] {
    override val mt: MonadThrow[Future] = MonadThrow.apply[Future]
    override def get(key: String): F[V] = base.get(key) match {
      case Some(x) => Future.successful(x)
      case None => Future.failed(new NoSuchElementException(s"Element $key not found"))
    }
    override def iterator: MonadicIterator[F, (String, V)] = MonadicIterator.from_iterator(base.iterator)
    override def updated[V1 >: V](key: String, value: V1):  Context[F, V1] =
      new MapBasedFutureContext(base.updated(key, value))
    override def removed(key: String): Context[F, V] =
      new MapBasedFutureContext(base.removed(key))
  }

  def fromMap_Future[V](base: Map[String, V])(implicit ec: ExecutionContext): Context[Future, V] =
    new MapBasedFutureContext[V](base)
  def fromMap_Future[V]()(implicit ec: ExecutionContext): Context[Future, V] =
    fromMap_Future(Map[String, V]())

  import cats.instances.either._
  type EitherRes[+T] = Either[Throwable, T]
  class MapBasedTryContext[V](base: Map[String, V]) extends Context[EitherRes, V] {
    override val mt: MonadThrow[EitherRes] = MonadThrow.apply[EitherRes]
    override def get(key: String): F[V] = base.get(key) match {
      case Some(x) => Right(x)
      case None => Left(new NoSuchElementException(s"Element $key not found"))
    }
    override def iterator: MonadicIterator[F, (String, V)] = MonadicIterator.from_iterator(base.iterator)
    override def updated[V1 >: V](key: String, value: V1):  Context[F, V1] =
      new MapBasedTryContext(base.updated(key, value))
    override def removed(key: String): Context[F, V] =
      new MapBasedTryContext(base.removed(key))
  }

  def fromMap_Either[V](base: Map[String, V]): Context[EitherRes, V] =
    new MapBasedTryContext[V](base)
  def fromMap_Either[V]()(implicit ec: ExecutionContext): Context[EitherRes, V] =
    fromMap_Either(Map[String, V]())

}


