package sysmo.reform.server

import cats.MonadThrow
import sysmo.reform.shared.query.SQLQueryService
import sysmo.reform.shared.runtime.RFRuntime
import sysmo.reform.shared.types.TypeSystem
import sysmo.reform.shared.containers.FLocal
import sysmo.reform.shared.logging.Printer
import sysmo.reform.shared.util.Injector
import sysmo.reform.storage.orientdb.OrientDBQueryService

trait OrientDBReformServer[_F[+_]] extends ReformServer[_F] {
  val type_system: TypeSystem

  val printer = new Printer {
    override def out(msg: String): Unit = println(msg)
    override def warn(msg: String): Unit = println(msg)
    override def error(msg: String): Unit = println(msg)
  }
  Injector.configure(printer)

  lazy val storage = sysmo.reform.storage.create_orientdb[F](
    config_storage.getConfig("orientdb")
  )

  def runtime: RFRuntime[F] = {
    val session = storage.session
    session.runtime(type_system)
  }

  def query_service: SQLQueryService[F] = {
    val session = storage.session
    session.query_service
  }
}
