package sysmo.reform.storage.orientdb

import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.record.OElement
import sysmo.reform.shared.data.{ObjectId, RecordFieldCodec, Value, ValueExtractor}
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, MultiReferenceType, PrimitiveDataType, RecordFieldType, ReferenceType}

import java.util.Date
import Value.implicits._
import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.{containers => C}

class DBCodec[_F[+_]](implicit val mt: MonadThrow[_F]) extends RecordFieldCodec[_F] {
  type Record = OElement


  override def read_value(field: RecordFieldType, record: Record): F[Value] = {
    val value = field.dtype match {
      case fdtype: PrimitiveDataType => fdtype match {
        case PrimitiveDataType.Real =>
          Value(record.getProperty[Double](field.name))
        case PrimitiveDataType.Int =>
          Value(record.getProperty[Int](field.name))
        case PrimitiveDataType.Long =>
          Value(record.getProperty[Long](field.name))
        case PrimitiveDataType.Char =>
          Value(record.getProperty[String](field.name))
        case PrimitiveDataType.Bool =>
          Value(record.getProperty[Boolean](field.name))
        case PrimitiveDataType.Date =>
          Value(record.getProperty[Date](field.name))
        case PrimitiveDataType.Id =>
          Value(OrientId(record.getProperty[ORID](field.name)))
      }
      case fdtype: CompoundDataType =>
        Value(OrientId(record.getProperty[ORID](field.name)))
      case fdtype: ArrayType =>
        Value(OrientId(record.getProperty[ORID](field.name)))
      case fdtype: ReferenceType =>
        Value(OrientId(record.getProperty[ORID](field.name)))
      case fdtype: MultiReferenceType =>
        Value(OrientId(record.getProperty[ORID](field.name)))
    }

    mt.pure(value)
  }

  override def write_value(field: RecordFieldType, record: Record, value: Value): F[Unit] = {
    val name = field.name
    def set[T: ValueExtractor](_value: Value): F[Unit] = C.catch_exception {
      _value.get[T] match {
        case Some(v) => mt.pure(record.setProperty(name, v))
        case None => mt.pure(record.removeProperty(name))
      }
    }

    def set_orid(_value: Value): F[Unit] = C.catch_exception {
      _value.get[ObjectId] match {
        case Some(v) => Util.ensure_orientid(v).map(oid => record.setProperty(name, oid))
        case None => mt.pure(record.removeProperty(name))
      }
    }

    field.dtype match {
      case fdtype: PrimitiveDataType => fdtype match {
        case PrimitiveDataType.Real => set[Double](value)
        case PrimitiveDataType.Int => set[Int](value)
        case PrimitiveDataType.Long => set[Long](value)
        case PrimitiveDataType.Char => set[String](value)
        case PrimitiveDataType.Bool => set[Boolean](value)
        case PrimitiveDataType.Date => set[Date](value)
        case PrimitiveDataType.Id => set_orid(value)
      }
      case fdtype: CompoundDataType => set_orid(value)
      case fdtype: ArrayType => set_orid(value)
      case fdtype: ReferenceType => set_orid(value)
      case fdtype: MultiReferenceType => set_orid(value)
    }
  }
}
