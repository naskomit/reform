package sysmo.reform.storage.orientdb

import cats.MonadThrow
import com.orientechnologies.orient.core.db.{ODatabaseSession, OrientDB, OrientDBConfig}
import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import sysmo.reform.shared.storage.{SchemaService, StorageSchema}
import sysmo.reform.shared.types.{ArrayType, PrimitiveDataType, CompoundDataType, MultiReferenceType, RecordFieldType, RecordType, ReferenceType}
import sysmo.reform.shared.logging.Logging

import scala.collection.immutable.Seq
import scala.jdk.CollectionConverters._

class SchemaImpl(klass: OClass) extends StorageSchema {
  override def name: String = klass.getName
}

class SchemaServiceImpl[F[+_]](sess: ODatabaseSession)(implicit mt: MonadThrow[F])
  extends SchemaService[F] with Logging {
  val oschema = sess.getMetadata.getSchema
  def list_schemas: Iterable[StorageSchema] = {
    oschema.getClasses.asScala
      .map(klass => new SchemaImpl(klass))
  }

  private def create_record_fields(klass: OClass, dtype: RecordType): F[Unit] = {
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
        case dtype: ArrayType => ???
        case dtype: ReferenceType => ???
        case dtype: MultiReferenceType => ???
      }
      klass.createProperty(rec_field.name, otype)
    }

    mt.pure()
  }

  override def add_record_schema(dtype: RecordType): F[Unit] = {
    if (oschema.existsClass(dtype.symbol)) {
      return mt.raiseError(new IllegalArgumentException(s"Class ${dtype.symbol} already exists in the database"))
    } else {
      logger.info(s"Creating class ${dtype.symbol}")
      val klass = oschema.createClass(dtype.symbol)
      klass.createProperty("$uuid", OType.STRING)
      create_record_fields(klass, dtype)
    }
    mt.pure()
  }
}
