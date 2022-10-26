package sysmo.reform.storage.orientdb

import cats.MonadThrow
import com.orientechnologies.orient.core.db.{ODatabaseSession, OrientDB, OrientDBConfig}
import com.typesafe.config.Config
import sysmo.reform.shared.logging.Logging
import sysmo.reform.shared.runtime.RFRuntime
import sysmo.reform.shared.storage.{SchemaService, Storage, StorageSession}
import sysmo.reform.shared.types.{RecordType, TypeSystem}

class StorageImpl[F[+_]](config: Config)(implicit val mt: MonadThrow[F])
  extends Storage[F] {
    override def session: SessionImpl[F] = {
      val dbname = config.getString("dbname")
      val user = config.getString("user")
      val password = config.getString("password")
      val uri = config.getString("uri")
      val db = new OrientDB(uri, OrientDBConfig.defaultConfig())
      val session: ODatabaseSession = db.open(dbname, user, password)
      new SessionImpl(session)
    }
}

class SessionImpl[F[+_]](session: ODatabaseSession)(implicit val mt: MonadThrow[F])
  extends StorageSession[F] with Logging {
  override def schema: SchemaService[F] = new SchemaServiceImpl[F](session)

  override def runtime(type_system: TypeSystem): RFRuntime[F] =
    new OrientDBRuntime[F](type_system, session)

  def with_transaction(f: => F[Unit]): F[Unit] = {
    session.begin()
    val res = f
    mt.map(res)(_ => session.commit())
    mt.onError(res){
      case e: Throwable => {
        session.rollback()
        logger.error("Aborting transaction!!!")
        mt.raiseError(e)
      }
    }
    res
  }
}