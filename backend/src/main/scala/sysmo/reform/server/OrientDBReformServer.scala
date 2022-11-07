package sysmo.reform.server

import cats.MonadThrow
import sysmo.reform.shared.query.QueryService
import sysmo.reform.shared.runtime.RFRuntime
import sysmo.reform.shared.types.TypeSystem
import sysmo.reform.shared.util.containers.FLocal
import sysmo.reform.storage.orientdb.OrientDBQueryService

trait OrientDBReformServer[_F[+_]] extends ReformServer[_F] {
  val type_system: TypeSystem
  val storage = sysmo.reform.storage.create_orientdb[F](
    config_storage.getConfig("orientdb")
  )

  def runtime: RFRuntime[F] = {
    val session = storage.session
    session.runtime(type_system)
  }

  def query_service: QueryService[F] = {
    val session = storage.session
    session.query_service
  }
}
