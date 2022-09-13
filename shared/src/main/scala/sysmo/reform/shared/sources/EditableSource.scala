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
  def dispatch[U <: SourceAction](action: U): F[Unit]
  def select(id: ObjectId): F[Unit]
}