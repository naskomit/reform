package sysmo.reform.shared.containers

import cats.MonadThrow

import scala.concurrent.Future

trait RFContainer[+T] {
  def to_future: Future[T]
}
