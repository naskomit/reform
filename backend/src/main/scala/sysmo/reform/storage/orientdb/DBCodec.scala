package sysmo.reform.storage.orientdb

import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.record.OElement
import sysmo.reform.shared.data.{ObjectId, Value, ValueExtractor}
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, MultiReferenceType, PrimitiveDataType, RecordFieldType, ReferenceType}

import java.util.Date
import Value.implicits._
import cats.MonadThrow
import cats.syntax.all._

class DBCodec[F[+_]](implicit val mt: MonadThrow[F]) {
  def ensure_orientid(id: ObjectId): F[OrientId] = {
    id match {
      case oid: OrientId => mt.pure(oid)
      case _ => mt.raiseError(new IllegalArgumentException(s"Not an OrientId: ${id}"))
    }
  }

  def value2orid(value: Value): F[Option[ORID]] =
    value.get[ObjectId] match {
      case Some(x) => ensure_orientid(x).map(oid => Some(oid.v))
      case None => mt.pure(None)
    }

  def read_value(field: RecordFieldType, element: OElement): Value = {
    field.dtype match {
      case fdtype: PrimitiveDataType => fdtype match {
        case PrimitiveDataType.Real =>
          Value(element.getProperty[Double](field.name))
        case PrimitiveDataType.Int =>
          Value(element.getProperty[Int](field.name))
        case PrimitiveDataType.Long =>
          Value(element.getProperty[Long](field.name))
        case PrimitiveDataType.Char =>
          Value(element.getProperty[String](field.name))
        case PrimitiveDataType.Bool =>
          Value(element.getProperty[Boolean](field.name))
        case PrimitiveDataType.Date =>
          Value(element.getProperty[Date](field.name))
        case PrimitiveDataType.Id =>
          Value(OrientId(element.getProperty[ORID](field.name)))
      }
      case fdtype: CompoundDataType =>
        Value(OrientId(element.getProperty[ORID](field.name)))
      case fdtype: ArrayType =>
        Value(OrientId(element.getProperty[ORID](field.name)))
      case fdtype: ReferenceType =>
        Value(OrientId(element.getProperty[ORID](field.name)))
      case fdtype: MultiReferenceType =>
        Value(OrientId(element.getProperty[ORID](field.name)))
    }
  }

  def write_value(element: OElement, field: RecordFieldType, value: Value): Unit = {
    val name = field.name
    def set[T: ValueExtractor](v: Option[T]): Unit = {
      value.get[T] match {
        case Some(v) => element.setProperty(name, v)
        case None => element.removeProperty(name)
      }
    }
    field.dtype match {
      case fdtype: PrimitiveDataType => fdtype match {
        case PrimitiveDataType.Real =>
          set(value.get[Double])
        case PrimitiveDataType.Int =>
          set(value.get[Int])
        case PrimitiveDataType.Long =>
          set(value.get[Long])
        case PrimitiveDataType.Char =>
          set(value.get[String])
        case PrimitiveDataType.Bool =>
          set(value.get[Boolean])
        case PrimitiveDataType.Date =>
          set(value.get[Date])
        case PrimitiveDataType.Id =>
          element.setProperty(name, value2orid(value).map(_.orNull))
      }
      case fdtype: CompoundDataType =>
        element.setProperty(name, value2orid(value).map(_.orNull))
      case fdtype: ArrayType =>
        element.setProperty(name, value2orid(value).map(_.orNull))
      case fdtype: ReferenceType =>
        element.setProperty(name, value2orid(value).map(_.orNull))
      case fdtype: MultiReferenceType =>
        element.setProperty(name, value2orid(value).map(_.orNull))
    }
  }
}
