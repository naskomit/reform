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

  def traverse[A](f: Seq[V] => A): F[A] =
    mt.map(
      fold_left(Seq[V]())((acc, elem) => acc :+ elem)
    )(f)

  def traverse(): F[Seq[V]] =
    fold_left(Seq[V]())((acc, elem) => acc :+ elem)

//  def filter(f: V => Boolean): MonadicIterator[F, V] = {
//    val source = this
//    new MonadicIterator[F, V] {
//      var _next: Option[V] = None
//      override val mt: MonadThrow[F] = source.mt
//      override def has_next: F[Boolean] = mt.tailRecM(true){x =>
//        source.has_next.map()
//      }
//
//      override def next: F[V] = {
//        val res = _next.get
//        _next = None
//        mt.pure(_next)
//      }
//    }
//  }
}

//trait LookAheadMonadicIterator[F[+_], +V] extends MonadicIterator[F, V] {
//  private var _lookahead: Seq[V] = Seq()
//  private def attempt_fetch
//  override def has_next: F[Boolean] = ???
//}

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

  def from_fiterator[F[+_], V](it: F[Iterator[V]])(implicit _mt: MonadThrow[F]): MonadicIterator[F, V] = {
    new MonadicIterator[F, V] {
      val mt = _mt
      override def has_next: F[Boolean] = mt.map(it)(x => x.hasNext)
      override def next: F[V] = mt.map(it)(x => x.next())
    }
  }

  def from_iteratorf[F[+_], V](it: Iterator[F[V]])(implicit _mt: MonadThrow[F]): MonadicIterator[F, V] = {
    new MonadicIterator[F, V] {
      val mt = _mt
      override def has_next: F[Boolean] = mt.pure(it.hasNext)
      override def next: F[V] = it.next
    }
  }
}