package sysmo.reform.shared.containers

import cats.MonadThrow

object implicits {
  lazy implicit val FLocal_mt: MonadThrow[FLocal] = FLocal.mt
  lazy implicit val FRemote_mt: MonadThrow[FRemote] = FRemote.mt
}
