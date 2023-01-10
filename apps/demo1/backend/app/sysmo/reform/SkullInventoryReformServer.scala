package sysmo.reform

import cats.MonadThrow
import sysmo.reform.server.OrientDBReformServer
import sysmo.reform.shared.containers.FLocal
import sysmo.reform.shared.examples.SkullInventoryBuilder
import sysmo.reform.shared.types.TypeSystem
import sysmo.reform.shared.containers.implicits._

object SkullInventoryReformServer extends OrientDBReformServer[FLocal] {
  override val mt = MonadThrow[FLocal]
  override val type_system: TypeSystem = SkullInventoryBuilder.type_builder.build
}
