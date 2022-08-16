package sysmo.reform.shared.util

import cats.MonadThrow

trait MonadicIterator[F[+_], +V] {
  def has_next: F[Boolean]
  def next: F[V]
  val mt: MonadThrow[F]

  def map[B](f: V => B): MonadicIterator[F, B] = {
    val it = this
    new MonadicIterator[F, B] {
      val mt = it.mt
      override def has_next: F[Boolean] = it.has_next
      override def next: F[B] = mt.map(it.next)(f)
    }
  }

  def flat_map[B](f: V => F[B]): MonadicIterator[F, B] = {
    val it = this
    new MonadicIterator[F, B] {
      val mt = it.mt
      override def has_next: F[Boolean] = it.has_next
      override def next: F[B] = mt.flatMap(it.next)(f)
    }
  }

  def fold_left[B](start: B)(f: (B, V) => B): F[B] = {
    mt.tailRecM(start)((acc: B) =>
      mt.flatMap(has_next)(hn =>
        if (hn)
          mt.map(next)(elem => Left(f(acc, elem)))
        else
          mt.pure(Right(acc))
      ))
  }
}

object MonadicIterator {
  def empty[F[+_], V](implicit _mt: MonadThrow[F]): MonadicIterator[F, V] = new MonadicIterator[F, V] {
    val mt = _mt
    override def has_next: F[Boolean] = mt.pure(false)
    override def next: F[V] = ???
  }

//  implicit class Ops[F[+_], V](val it: MonadicIterator[F, V])(implicit val mt: MonadThrow[F])
//    extends MonadicIteratorOps[F, V]

  def from_iterator[F[+_], V](it: Iterator[V])(implicit _mt: MonadThrow[F]): MonadicIterator[F, V] = {
    new MonadicIterator[F, V] {
      val mt = _mt
      override def has_next: F[Boolean] = mt.pure(it.hasNext)
      override def next: F[V] = mt.pure(it.next())
    }
  }
}