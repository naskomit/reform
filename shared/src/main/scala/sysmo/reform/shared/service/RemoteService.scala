package sysmo.reform.shared.service

import cats.MonadThrow
import io.circe.{Decoder, Encoder}
import sysmo.reform.shared.containers.FRemote

trait RemoteService[F[+_]] {
  type MethodId = String
  val mt: MonadThrow[F]
  def do_call[I: Encoder, O: Decoder](method_id: MethodId, req: I): F[O]
}
