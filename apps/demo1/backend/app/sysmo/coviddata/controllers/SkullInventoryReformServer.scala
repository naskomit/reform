package sysmo.coviddata.controllers

import cats.MonadThrow
import sysmo.reform.server.OrientDBReformServer
import sysmo.reform.shared.examples.SkullInventoryBuilder
import sysmo.reform.shared.types.TypeSystem
import sysmo.reform.shared.util.containers.FLocal

object SkullInventoryReformServer extends OrientDBReformServer[FLocal] {
  override implicit val mt = MonadThrow[FLocal]
  override val type_system: TypeSystem = SkullInventoryBuilder.type_builder.build
}
