package sysmo.reform.storage.orientdb

import cats.MonadThrow
import com.orientechnologies.orient.core.db.ODatabaseSession
import com.orientechnologies.orient.core.id.{ORID, ORecordId}
import com.orientechnologies.orient.core.metadata.schema.OProperty
import com.orientechnologies.orient.core.record.{OElement, ORecord}
import sysmo.reform.shared.data.{ObjectId, ObjectIdSupplier, UUObjectId, Value}
import sysmo.reform.shared.runtime.{ArrayInstance, Constructors, LocalObjects, LocalRuntime, ObjectProxy, RFObject, RFRuntime, RecordInstance, RuntimeAction}
import sysmo.reform.shared.table.Query
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, DataType, MultiReferenceType, PrimitiveDataType, RecordType, ReferenceType, TypeSystem}
import sysmo.reform.shared.util.MonadicIterator

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

  override def get(id: ObjectId): F[RTO] = {
    id match {
      case oid: OrientId => {
        val loaded: OElement = session.load(oid.v)
        if (loaded == null) {
          mt.raiseError(new IllegalArgumentException(s"Object ${id} not found in the database"))
        } else {
          val class_name = loaded.getSchemaType.get().getName
          val parent_id_raw = loaded.getProperty[ORID]("$$parent_id") match {
            case x => Some(x)
            case null => None
          }

          val parent_id = parent_id_raw.map(x => OrientId(x))

          type_system.get(class_name) match {
            case Some(rdt: RecordType) =>
              mt.pure(LocalObjects.record(rdt, id, parent_id))
            case None => mt.raiseError(new IllegalArgumentException(
              s"Database class ${class_name} is not found in the runtime type system!"
            ))
          }
        }
      }
      case _ => mt.raiseError(new IllegalArgumentException(s"Not an OrientId: ${id}"))
    }
  }



  override def create_record(dtype: RecordType, parent: Option[ObjectId]): F[RecordInstance[F]] = ???

  override def create_array(dtype: ArrayType, parent: Option[ObjectId]): F[ArrayInstance[F]] = ???

  override def list: MonadicIterator[F, ObjectProxy] = ???
  override def count: F[Int] = ???
  override def count(q: Query): F[Int] = ???
  override def run_query(q: Query): MonadicIterator[F, RFObject[F]] = ???
  override def dispatch(action: RuntimeAction): F[Unit] = ???


//  override def create(dtype: DataType): F[ObjectId] = {
//    dtype match {
//      case dt: RecordType => {
//        val db_instance: OElement = session.newInstance(dt.symbol)
//        val db_rec: ORecord = db_instance.save()
//        mt.pure(OrientId(db_rec))
//      }
//      case dt: ArrayType => ???
//      case _ => mt.raiseError(
//        new IllegalArgumentException(s"Can not create instance of type ${dtype}")
//      )
//    }
//  }

  override def update_record(id: ObjectId, field_values: Seq[(String, Value)]): F[RecordInstance[F]] = ???
  override def remove(id: ObjectId): F[Unit] = ???
}

object OrientDBRuntime {

}