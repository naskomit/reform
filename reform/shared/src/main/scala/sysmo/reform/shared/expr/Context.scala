package sysmo.reform.shared.expr

import cats.MonadThrow

trait Context[_F[+_], +V] {
  type F[+X] = _F[X]
  val mt: MonadThrow[F]
  def get(key: String): F[V]
  def iterator: Iterator[(String, V)]
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
    override def iterator: Iterator[(String, V)] = base.iterator
    override def updated[V1 >: V](key: String, value: V1):  Context[F, V1] =
      new MapBasedFutureContext(base.updated(key, value))
    override def removed(key: String): Context[F, V] =
      new MapBasedFutureContext(base.removed(key))
  }

  def fromMap[V](base: Map[String, V])(implicit ec: ExecutionContext): Context[Future, V] =
    new MapBasedFutureContext[V](base)
  def fromMap[V]()(implicit ec: ExecutionContext): Context[Future, V] =
    fromMap(Map[String, V]())
}


