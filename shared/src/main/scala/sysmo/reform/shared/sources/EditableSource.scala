package sysmo.reform.shared.sources

import cats.MonadThrow
import sysmo.reform.shared.data.ObjectId
import sysmo.reform.shared.runtime.RuntimeAction

trait EditableSource[F[+_]] {
  def dispatcher: Dispatcher[F]
}

trait SourceAction extends RuntimeAction

trait Dispatcher[F[+_]] {
  implicit val mt: MonadThrow[F]
  def dispatch(action: RuntimeAction): F[Unit]
  def select(id: ObjectId): F[Unit]
  def tap(f: RuntimeAction => F[Unit]): Dispatcher[F] = {
    new TappedDispatcher[F](this, f)
  }
}

class TappedDispatcher[F[+_]] (base: Dispatcher[F], f: RuntimeAction => F[Unit]) extends Dispatcher[F] {
  override implicit val mt: MonadThrow[F] = base.mt
  override def dispatch(action: RuntimeAction): F[Unit] = {
    mt.flatMap(base.dispatch(action))(_ => f(action))
  }
  override def select(id: ObjectId): F[Unit] = base.select(id)
}