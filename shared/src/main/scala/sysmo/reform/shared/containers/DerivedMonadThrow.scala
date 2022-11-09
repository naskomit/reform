package sysmo.reform.shared.containers

import cats.MonadThrow

trait DerivedMonadThrow[Outer[+_], Inner[+_]] extends MonadThrow[Outer] {
  val mt_inner: MonadThrow[Inner]
  def construct[A](i: Inner[A]): Outer[A]
  def extract[A](o: Outer[A]): Inner[A]

  override def pure[A](x: A): Outer[A] =
    construct(mt_inner.pure(x))

  override def map[A, B](fa: Outer[A])(f: A => B): Outer[B] =
    construct(mt_inner.map(extract(fa))(f))
  override def flatMap[A, B](fa: Outer[A])(f: A => Outer[B]): Outer[B] =
    construct(mt_inner.flatMap(extract(fa))(x => extract(f(x))))
  override def tailRecM[A, B](a: A)(f: A => Outer[Either[A, B]]): Outer[B] =
    construct(mt_inner.tailRecM(a)(x => extract(f(x))))
  override def raiseError[A](e: Throwable): Outer[A] =
    construct(mt_inner.raiseError(e))
  override def handleErrorWith[A](fa: Outer[A])(f: Throwable => Outer[A]): Outer[A] =
    construct(mt_inner.handleErrorWith(extract(fa))(x => extract(f(x))))

}
