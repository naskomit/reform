package sysmo.reform.storage.orientdb

import cats.MonadThrow
import cats.syntax.all._
import com.orientechnologies.orient.core.db.ODatabaseSession
import com.orientechnologies.orient.core.id.{ORID, ORecordId}
import com.orientechnologies.orient.core.metadata.schema.OProperty
import com.orientechnologies.orient.core.record.{OElement, ORecord}
import sysmo.reform.shared.data.{ObjectId, ObjectIdSupplier, UUObjectId, Value, ValueConstructor, ValueExtractor}
import sysmo.reform.shared.runtime.{ArrayInstance, Constructors, LocalObjects, LocalRuntime, ObjectProxy, RFObject, RFRuntime, RecordInstance, RuntimeAction}
import sysmo.reform.shared.table.{BasicQuery, Query, Table}
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, DataType, MultiReferenceType, PrimitiveDataType, RecordFieldType, RecordType, ReferenceType, TypeSystem}
import sysmo.reform.shared.util.MonadicIterator
import Value.implicits._

import java.util.Date
import scala.jdk.CollectionConverters._

class OrientId(orid: ORID) extends ObjectId {
  override type Id = ORID
  override val v: ORID = orid
  override def next: ObjectId = ???
  override def show: String = orid.toString
}

object OrientId {
  def apply(orid: ORID): OrientId = new OrientId(orid)
  def apply(db_rec: ORecord): OrientId = OrientId(db_rec.getIdentity)
}

class OrientDBRuntime[_F[+_]](val type_system: TypeSystem, session: ODatabaseSession)(implicit val mt: MonadThrow[_F])
  extends RFRuntime[_F] {
  override val constructors: Constructors[F] = new Constructors[F] {
    override def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordInstance[F]] =
      mt.pure(LocalObjects.record(dtype, id, parent))
    override def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayInstance[F]] =
      mt.pure(LocalObjects.array(dtype, id, parent))
  }

  private object Aux {
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

    def ensure_not_null[T](x: T, msg: String): F[T] = {
      if (x == null) {
        mt.raiseError(new IllegalArgumentException(msg))
      } else {
        mt.pure(x)
      }
    }

    def load_element(oid: OrientId): F[OElement] = {
      ensure_not_null(session.load(oid.v): OElement,
        s"Object ${oid} not found in the database")
    }

    def get_parent_id(element: OElement): Option[OrientId] = {
      val parent_id_raw = element.getProperty[ORID]("$$parent_id") match {
        case x => Some(x)
        case null => None
      }
      parent_id_raw.map(x => OrientId(x))
    }

    def get_record_type(element: OElement): F[RecordType] = {
      val class_name = element.getSchemaType.get().getName
      type_system.get(class_name) match {
        case Some(rdt: RecordType) => mt.pure(rdt)
        case None => mt.raiseError(new IllegalArgumentException(
          s"Database class ${class_name} is not found in the runtime type system!"
        ))
      }
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

  override def get(id: ObjectId): F[RTO] = {
    for {
      oid <- Aux.ensure_orientid(id)
      element <- Aux.load_element(oid)
      dtype <- Aux.get_record_type(element)
      instance <- Util.catch_exception {
        val parent_id = Aux.get_parent_id(element)
        val inst = LocalObjects.record(dtype, id, parent_id)(mt)
        for (field <- dtype.fields) {
          val value = Aux.read_value(field, element)
          inst.set_field(field.name, value)
        }
        inst
      }
    } yield instance
  }

  override def create_record(dtype: RecordType, parent: Option[ObjectId]): F[RecordInstance[F]] = {
    val element: OElement = session.newInstance(dtype.symbol)
    def insert(el: OElement): F[RecordInstance[F]] = {
      val orec: ORecord = el.save()
      constructors.record(dtype, OrientId(orec.getIdentity), parent)
    }
    parent match {
      case Some(p: OrientId) => {
        element.setProperty("$$parent", p.v)
        insert(element)
      }
      case Some(p) => mt.raiseError(
        new IllegalArgumentException("Parent is not an orient ID")
      )
      case None => {
        insert(element)
      }
    }
  }

  override def create_array(dtype: ArrayType, parent: Option[ObjectId]): F[ArrayInstance[F]] = ???

  override def update_record(id: ObjectId, field_values: Seq[(String, Value)]): F[RecordInstance[F]] = {
    for {
      oid <- Aux.ensure_orientid(id)
      element <- Aux.load_element(oid)
      rec_type <- Aux.get_record_type(element)
      _ <- field_values.traverse { fv: (String, Value) =>
        val (name, value) = fv
        val field_type = rec_type.field(name)
        field_type match {
          case Some(ftype) => {
            Util.catch_exception(
              Aux.write_value(element, ftype, value)
            )
          }
          case None => mt.raiseError(new IllegalArgumentException(
            s"No field ${name} in record type ${rec_type.symbol}"
          ))
        }
      }
      new_rec <- {
        element.save()
        get_typed[RecordInstance[F]](id)
      }
    } yield new_rec
  }

  override def remove(id: ObjectId): F[Unit] = {
    for {
      oid <- Aux.ensure_orientid(id)
      res <- Util.catch_exception(
        session.delete(oid.v)
      )
    } yield res
  }



  override def list: MonadicIterator[F, ObjectProxy] = ???
  override def count: F[Int] = ???
  override def count(q: Query): F[Int] = ???
  override def run_query(q: Query): MonadicIterator[F, RFObject[F]] = ???


  override def run_table_query(q: Query): F[Table[F]] = ???
//  {
////    q match {
////      case BasicQuery(SingleTable(id, alias, schema), projection, filter, sort, range) => ???
////    }
//
//    val statement = "select @rid, code, sex, age, image_type, BMI, `Filter 1`, `Filter 2` from `SkullEntry`"
//    val arguments = Seq()
//    val result_set = session.query(statement, arguments:_*)
//    result_set.asScala.map(item => item)
//    val runtime = this
//    new Table[F] {
//      override implicit val mt: MonadThrow[F] = runtime.mt
//      override def schema: RecordType = ???
//      override def nrow: F[Int] = ???
//      override def row_iter: MonadicIterator[F, Table.Row] = ???
//    }
//  }

  override def dispatch(action: RuntimeAction): F[Unit] = ???



}

object OrientDBRuntime {

}