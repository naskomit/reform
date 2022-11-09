package sysmo.reform.storage.orientdb

import cats.MonadThrow
import cats.syntax.all._
import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import sysmo.reform.shared.storage.{SchemaService, StorageSchema}
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, MultiReferenceType, PrimitiveDataType, RecordFieldType, RecordType, ReferenceType, TypeSystem}
import sysmo.reform.shared.logging.Logging
import sysmo.reform.shared.{containers => C}
import scala.jdk.CollectionConverters._

class SchemaImpl(klass: OClass) extends StorageSchema {
  override def name: String = klass.getName
}

abstract class SchemaServiceError(msg: String = "", cause: Throwable)
  extends Throwable(msg, cause)
class EntityDuplicationError(msg: String, cause: Throwable = None.orNull)
  extends SchemaServiceError(msg, cause)

class SchemaServiceImpl[F[+_]](session: SessionImpl[F])(implicit mt: MonadThrow[F])
  extends SchemaService[F] with Logging {
  private val oschema = session.db_session.getMetadata.getSchema
  private def klasses: Iterable[OClass] = oschema.getClasses.asScala

  def list_schemas: F[Iterable[StorageSchema]] = {
    C.catch_exception(
      mt.pure(klasses
        .map(klass => new SchemaImpl(klass))
    ))
  }

  override def has_schema(name: String): F[Boolean] = {
    C.catch_exception {
      mt.pure(oschema.existsClass(name))
    }
  }

  override def ensure_schema(name: String): F[Unit] =
    has_schema(name).flatMap {
      case true => {
        logger.info(s"Schema ${name} is present")
        mt.pure()
      }
      case false => C.catch_exception {
        logger.info(s"Creating schema ${name}")
        oschema.createClass(name)
        mt.pure()
      }
    }

  def ensure_base_schemas(): F[Unit] = {
    for {
      _ <- ensure_schema("RecordType")
      _ <- ensure_schema("ArrayType")
      res <- mt.pure()
    } yield res
  }

  private def create_record_fields(klass: OClass, dtype: RecordType): F[Unit] = {
    C.catch_exception {
      dtype.fields.foreach {rec_field =>
        val otype = rec_field.dtype match {
          case dtype: PrimitiveDataType => dtype match {
            case PrimitiveDataType.Real => OType.DOUBLE
            case PrimitiveDataType.Int => OType.INTEGER
            case PrimitiveDataType.Long => OType.LONG
            case PrimitiveDataType.Char => OType.STRING
            case PrimitiveDataType.Bool => OType.BOOLEAN
            case PrimitiveDataType.Date => OType.DATE
            case PrimitiveDataType.Id => OType.STRING
          }
          case dtype: CompoundDataType => ???
          case dtype: ArrayType => OType.LINK
          case dtype: ReferenceType => ???
          case dtype: MultiReferenceType => ???
        }
        klass.createProperty(rec_field.name, otype)
      }
      mt.pure()
    }
  }

  override def add_record_schema(record_type: RecordType): F[Unit] = {
    C.catch_exception {
      if (oschema.existsClass(record_type.symbol)) {
        return mt.raiseError(new EntityDuplicationError(s"Class ${record_type.symbol} already exists in the database"))
      } else {
        logger.info(s"Creating class ${record_type.symbol}")
        val klass = oschema.createClass(record_type.symbol)
        klass.createProperty("$$parent", OType.LINK)
        create_record_fields(klass, record_type)
      }
    }
  }

  override def ensure_record_schema(record_type: RecordType): F[Unit] = {
    if (oschema.existsClass(record_type.symbol)) {
      logger.info(s"Schema ${record_type.symbol} is present")
      mt.pure()
    } else {
      // Create
      add_record_schema(record_type).onError {
        case e: EntityDuplicationError => mt.pure()
      }
    }
  }

  override def sync(type_system: TypeSystem): F[Unit] = {
    for {
      _ <- ensure_base_schemas()
      // Sync records
      _ <- type_system.records.toSeq
        .traverse(record_type => ensure_record_schema(record_type))
        .map(_ => ())
      res <- mt.pure()
    } yield res
  }
}
