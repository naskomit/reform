package sysmo.reform.storage.orientdb

import cats.MonadThrow
import com.orientechnologies.orient.core.db.{ODatabaseSession, OrientDB, OrientDBConfig}
import com.typesafe.config.Config
import sysmo.reform.shared.logging.Logging
import sysmo.reform.shared.runtime.RFRuntime
import sysmo.reform.shared.storage.{SchemaService, Storage, StorageSession}
import sysmo.reform.shared.types.{TypeSystem}

class StorageImpl[F[+_]](config: Config)(implicit val mt: MonadThrow[F])
  extends Storage[F] with Logging {
    override def session: SessionImpl[F] = {
      val dbname = config.getString("dbname")
      val user = config.getString("user")
      val password = config.getString("password")
      val uri = config.getString("uri")
      val db = new OrientDB(uri, OrientDBConfig.defaultConfig())
      logger.info(s"Connecting to ${uri}/${dbname} with user ${user}")
      val session: ODatabaseSession = db.open(dbname, user, password)
      new SessionImpl(session)
    }
}

class SessionImpl[F[+_]](val db_session: ODatabaseSession)(implicit val mt: MonadThrow[F])
  extends StorageSession[F] with Logging {

  override type FieldCodec = DBCodec[F]
  override lazy val rec_field_codec: DBCodec[F] = new DBCodec[F]

  override type QService = OrientDBQueryService[F]
  override lazy val query_service = new OrientDBQueryService[F](this)

  override lazy val schema: SchemaService[F] = new SchemaServiceImpl[F](this)

  override def runtime(type_system: TypeSystem): RFRuntime[F] =
    new OrientDBRuntime[F](type_system, this)

  def with_transaction(f: => F[Unit]): F[Unit] = {
    db_session.begin()
    val res = f
    mt.map(res)(_ => db_session.commit())
    mt.onError(res){
      case e: Throwable => {
        db_session.rollback()
        logger.error("Aborting transaction!!!")
        mt.raiseError(e)
      }
    }
    res
  }
}