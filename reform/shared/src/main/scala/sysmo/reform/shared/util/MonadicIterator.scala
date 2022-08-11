package sysmo.reform.shared.util

import cats.MonadThrow

trait MonadicIterator[F[+_], +V] {
  def has_next: F[Boolean]
  def next: F[V]
}

trait MonadicIteratorOps[F[+_], A] {
  val mt: MonadThrow[F]
  def map[B](x: MonadicIterator[F, A])(f: A => B): MonadicIterator[F, B] = {
    new MonadicIterator[F, B] {
      override def has_next: F[Boolean] = x.has_next
      override def next: F[B] = mt.map(x.next)(f)
    }
  }
}

object MonadicIterator {
  implicit class Ops[F[+_], V](it: MonadicIterator[F, V])(implicit val mt: MonadThrow[F]) extends MonadicIteratorOps[F, V]
  def from_iterator[F[+_], V](it: Iterator[V])(implicit mt: MonadThrow[F]): MonadicIterator[F, V] = {
    new MonadicIterator[F, V] {
      override def has_next: F[Boolean] = mt.pure(it.hasNext)
      override def next: F[V] = mt.pure(it.next())
    }
  }
}